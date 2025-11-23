package me.hyunlee.laundry.payment.application.method

import me.hyunlee.laundry.common.application.idempotency.IdempotencyServicePort
import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.payment.application.port.`in`.customer.CustomerProviderPort
import me.hyunlee.laundry.payment.application.port.`in`.method.*
import me.hyunlee.laundry.payment.application.port.`in`.method.RetrievedPmInfo.*
import me.hyunlee.laundry.payment.application.port.out.method.PaymentMethodRepository
import me.hyunlee.laundry.payment.domain.exception.PaymentMethodException.*
import me.hyunlee.laundry.payment.domain.model.method.NewAchSpec
import me.hyunlee.laundry.payment.domain.model.method.NewCardSpec
import me.hyunlee.laundry.payment.domain.model.method.NewWalletSpec
import me.hyunlee.laundry.payment.domain.model.method.PaymentMethod
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentMethodCommandService(
    private val repo: PaymentMethodRepository,
    private val paymentMethodPort: PaymentMethodProviderPort,
    private val customerPort: CustomerProviderPort,
    private val idemPort: IdempotencyServicePort,
) : PaymentMethodCommandUseCase {

    private val log: Logger = LoggerFactory.getLogger(PaymentMethodCommandService::class.java)

    @Transactional
    override fun create(cmd: CreatePaymentMethodCommand): PaymentMethod {
        // Idempotent guard: prevent duplicate registration for same (user, provider_pm_id)
        if (repo.existsByUserAndProviderPmId(cmd.userId, cmd.stripePmId)) throw AlreadyExists(cmd.userId.toString(), cmd.stripePmId)

        // Ensure Stripe customer exists (delegated to StripePort impl or User integration behind it)
        val customerId = customerPort.ensureCustomerId(cmd.userId.toString())

        // Confirm SetupIntent to attach pm_* to customer and authorize off_session usage
        val setupStatus = paymentMethodPort.confirmPaymentMethodSetupIntent(customerId, cmd.stripePmId, "card", cmd.idempotentKey)

        // Accept both succeeded and requires_action (server cannot complete actions)
        if (setupStatus != "succeeded" && setupStatus != "requires_action") throw SetupIntentFailed(setupStatus, cmd.stripePmId)

        if (cmd.setAsDefault) repo.unsetDefaultForUser(cmd.userId)

        val pm = PaymentMethod.create(cmd.toSpec())

        return repo.save(pm)
    }

    @Transactional
    override fun startSetupIntent(
        userId: UserId,
        idempotentKey: String
    ): StartSetupIntentResult {
        val serverIdemKey = "pm_setup:$idempotentKey"

        return idemPort.execute(
            userId = userId.value,
            key = serverIdemKey,
            resourceType = "PM_SETUP",
            responseType = StartSetupIntentResult::class.java
        ) {
            val customerId = customerPort.ensureCustomerId(userId.value.toString())

            val si = paymentMethodPort.createPaymentMethodSetupIntent(
                customerId = customerId,
                usage = "off_session",
                idempotencyKey = serverIdemKey  // Stripe에도 동일 키 전달
            )

            val result = StartSetupIntentResult(
                setupIntentId = si.id,
                clientSecret = si.clientSecret,
                customerId = si.customerId
            )

            si.id to result
        }
    }

    @Transactional
    override fun finalizeSetupIntent(
        userId: UserId,
        setupIntentId: String,
        nickname: String?,
        setAsDefault: Boolean
    ): PaymentMethod {
        val si = paymentMethodPort.retrievePaymentMethodSetupIntent(setupIntentId)
        val pmId = requireNotNull(si.paymentMethodId) { "payment_method missing on SetupIntent" }

        // checkUserOwnsStripeCustomer(userId, si.customerId) // 필요 시 구현
        repo.findByUserAndProviderPmId(userId, pmId)?.let { existing ->
            return ensureDefaultIfRequested(userId, existing, setAsDefault)
        }

        // Fetch latest PM details from Stripe
        val info = paymentMethodPort.retrievePaymentMethodInfo(pmId)
        validateByType(info, si.status!!)

        // Fingerprint-based idempotency (Card/Wallet): if same card already exists for this user, reuse it
        findByFingerprintIfAny(userId, info)?.let { existing ->
            val refreshed = existing.refreshFrom(info)
            val saved = repo.save(refreshed)
            return ensureDefaultIfRequested(userId, saved, setAsDefault)
        }

        val created = createDomain(userId, pmId, info, si.mandateId, nickname)
        val saved = saveWithIdempotency(userId, created, info, pmId)
        return ensureDefaultIfRequested(userId, saved, setAsDefault)
    }

    private fun saveWithIdempotency(
        userId: UserId,
        created: PaymentMethod,
        info: RetrievedPmInfo,
        providerPmId: String
    ): PaymentMethod =
        try { repo.save(created) }
        catch (e: DataIntegrityViolationException) {
            findByFingerprintIfAny(userId, info)
                ?: repo.findByUserAndProviderPmId(userId, providerPmId)
                ?: throw e
        }

    private fun findByFingerprintIfAny(userId: UserId, info: RetrievedPmInfo): PaymentMethod? {
        val fp = when (info) {
            is Card -> info.fingerprint
            is Wallet -> info.fingerprint
            is Ach -> null
        }
        return fp?.takeIf { it.isNotBlank() }?.let { repo.findByUserAndFingerprint(userId, it) }
    }

    private fun validateByType(info: RetrievedPmInfo, status: String) {
        when (info) {
            is Card, is Wallet -> require(status == "succeeded" || status == "processing") { "SetupIntent must be succeeded/processing for card/wallet, current=${status}" }
            is Ach -> require(status in setOf("succeeded", "processing", "requires_action", "pending_verification"))
                        { "SetupIntent not ready for ACH, current=${status}" }
        }
    }

    private fun ensureDefaultIfRequested(userId: UserId, pm: PaymentMethod, setAsDefault: Boolean): PaymentMethod {
        if (!setAsDefault) return pm
        repo.makeDefault(userId, pm.id)
        return pm.markDefault()
    }

    private fun createDomain(userId: UserId, pmId: String, info: RetrievedPmInfo, mandate: String?, nickname: String?): PaymentMethod =
        when (info) {
            is Card -> PaymentMethod.create(
                NewCardSpec(
                    userId = userId,
                    stripePmId = pmId,
                    summary = info.summary,
                    fingerprint = info.fingerprint,
                    isDefault = false,
                    nickname = nickname
                )
            )

            is Wallet -> PaymentMethod.create(
                NewWalletSpec(
                    userId = userId,
                    stripePmId = pmId,
                    wallet = info.wallet,
                    summary = info.summary,
                    fingerprint = info.fingerprint,
                    isDefault = false,
                    nickname = nickname
                )
            )

            is Ach -> {
                val mandateId = requireNotNull(mandate) { "mandateId required" }
                PaymentMethod.create(
                    NewAchSpec(
                        userId = userId,
                        stripePmId = pmId,
                        bankName = info.bankName,
                        last4 = info.last4,
                        mandateId = mandateId,
                        verification = info.verification,
                        isDefault = false,
                        nickname = nickname
                    )
                )
            }
        }

    @Transactional
    override fun setDefault(userId: UserId, paymentMethodId: PaymentMethodId) {
        val pm = repo.findById(paymentMethodId) ?: throw NotFound(paymentMethodId.toString())

        if (pm.userId != userId) throw AccessDenied(userId.toString(), paymentMethodId.toString())

        repo.unsetDefaultForUser(userId)

        repo.setDefaultForUser(userId, paymentMethodId)
    }

    @Transactional
    override fun unsetDefault(userId: UserId, paymentMethodId: PaymentMethodId) {

        val pm = repo.findById(paymentMethodId) ?: return

        if (pm.userId != userId) throw AccessDenied(userId.toString(), paymentMethodId.toString())

        repo.unsetDefaultForUser(userId, paymentMethodId)
    }

}