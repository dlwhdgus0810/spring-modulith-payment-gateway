package me.hyunlee.laundry.payment.application.method

import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.common.domain.event.payment.ProviderCustomerEnsuredEvent
import me.hyunlee.laundry.payment.application.port.`in`.method.RetrievedPmInfo.*
import me.hyunlee.laundry.payment.application.port.out.method.PaymentMethodRepository
import me.hyunlee.laundry.payment.application.port.`in`.method.CreatePaymentMethodCommand
import me.hyunlee.laundry.payment.application.port.`in`.method.PaymentMethodCommandUseCase
import me.hyunlee.laundry.payment.application.port.`in`.method.PaymentMethodProviderPort
import me.hyunlee.laundry.payment.application.port.`in`.customer.CustomerProviderPort
import me.hyunlee.laundry.payment.application.port.`in`.method.RetrievedPmInfo
import me.hyunlee.laundry.payment.application.port.`in`.method.StartSetupIntentResult
import me.hyunlee.laundry.payment.domain.exception.method.PaymentMethodException.*
import me.hyunlee.laundry.payment.domain.model.method.NewAchSpec
import me.hyunlee.laundry.payment.domain.model.method.NewCardSpec
import me.hyunlee.laundry.payment.domain.model.method.NewWalletSpec
import me.hyunlee.laundry.payment.domain.model.method.PaymentMethod
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentMethodCommandService(
    private val repo: PaymentMethodRepository,
    private val methodProvider: PaymentMethodProviderPort,
    private val customerProvider: CustomerProviderPort,
    private val publisher: ApplicationEventPublisher
) : PaymentMethodCommandUseCase {

    private val log: Logger = LoggerFactory.getLogger(PaymentMethodCommandService::class.java)

    @Transactional
    override fun create(cmd: CreatePaymentMethodCommand): PaymentMethod {
        // Idempotent guard: prevent duplicate registration for same (user, provider_pm_id)
        if (repo.existsByUserAndProviderPmId(cmd.userId, cmd.stripePmId)) throw AlreadyExists(cmd.userId.toString(), cmd.stripePmId)

        // Ensure Stripe customer exists (delegated to StripePort impl or User integration behind it)
        val customerId = ensureCustomerId(cmd.userId)

        // Confirm SetupIntent to attach pm_* to customer and authorize off_session usage
        val setupStatus = methodProvider.confirmSetupIntent(customerId, cmd.stripePmId, "card", cmd.idempotentKey)

        // Accept both succeeded and requires_action (server cannot complete actions)
        if (setupStatus != "succeeded" && setupStatus != "requires_action") throw SetupIntentFailed(setupStatus, cmd.stripePmId)

        if (cmd.setAsDefault) repo.unsetDefaultForUser(cmd.userId)

        val pm = PaymentMethod.create(cmd.toSpec())

        return repo.save(pm)
    }

    @Transactional
    override fun startSetupIntent(
        userId: UserId,
        paymentMethodType: String,
        idempotentKey: String?
    ): StartSetupIntentResult {
        val customerId = ensureCustomerId(userId)

        val si = methodProvider.createSetupIntent(customerId, paymentMethodType, "off_session", idempotentKey)

        return StartSetupIntentResult(
            setupIntentId = si.id,
            clientSecret = si.clientSecret,
            customerId = si.customerId
        )
    }

    private fun ensureCustomerId(userId: UserId): String {
        val customerId = customerProvider.ensureCustomer(userId.toString())
        publisher.publishEvent(ProviderCustomerEnsuredEvent(userId, customerId))
        return customerId
    }

    @Transactional
    override fun finalizeSetupIntent(
        userId: UserId,
        setupIntentId: String,
        nickname: String?,
        setAsDefault: Boolean
    ): PaymentMethod {
        val si = methodProvider.retrieveSetupIntent(setupIntentId)
        val pmId = requireNotNull(si.paymentMethodId) { "payment_method missing on SetupIntent" }

        // checkUserOwnsStripeCustomer(userId, si.customerId) // 필요 시 구현
        repo.findByUserAndProviderPmId(userId, pmId)?.let { existing ->
            return ensureDefaultIfRequested(userId, existing, setAsDefault)
        }

        // Fetch latest PM details from Stripe
        val info = methodProvider.retrievePaymentInfo(pmId)
        validateByType(info, si.status!!)

        // Fingerprint-based idempotency (Card/Wallet): if same card already exists for this user, reuse it
        findByFingerprintIfAny(userId, info)?.let { existing ->
            val refreshed = existing.refreshFrom(info)
            val saved = repo.save(refreshed)
            return ensureDefaultIfRequested(userId, saved, setAsDefault)
        }

        val created = createDomain(userId, pmId, info, nickname)
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

    private fun createDomain(userId: UserId, pmId: String, info: RetrievedPmInfo, nickname: String?): PaymentMethod =
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

            is Ach -> PaymentMethod.create(
                NewAchSpec(
                    userId = userId,
                    stripePmId = pmId,
                    bankName = info.bankName,
                    last4 = info.last4,
                    mandateId = "",          // nullable 권장
                    verification = info.verification,
                    isDefault = false,
                    nickname = nickname
                )
            )
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



//    2) 커스텀 Outbox 테이블 구성 (세밀 제어·외부 연동용)
//
//    Stripe/Kafka/외부 API 전송을 트랜잭션과 분리해 확정적으로 저장하고, 별도 워커가 재시도/백오프/사이드이펙트를 수행하는 전통 패턴입니다.
//
//    핵심 아이디어
//    •	도메인 트랜잭션 내(BEFORE_COMMIT 권장) 에 Outbox 레코드를 쓰기만 한다.
//    •	커밋 후 별도 스케줄러/워커가 Outbox를 읽어 전송 시도 → 성공 시 SENT, 실패 시 RETRY/FAILED 로 상태 전이.
}