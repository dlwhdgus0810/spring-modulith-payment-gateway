package me.hyunlee.laundry.payment.application.transaction

import io.micrometer.core.instrument.MeterRegistry
import me.hyunlee.laundry.common.application.idempotency.IdempotencyServicePort
import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.monitoring.adapter.out.slack.SlackNotifier
import me.hyunlee.laundry.payment.application.port.`in`.customer.CustomerProviderPort
import me.hyunlee.laundry.payment.application.port.`in`.transaction.*
import me.hyunlee.laundry.payment.application.port.out.method.PaymentMethodRepository
import me.hyunlee.laundry.payment.application.port.out.transaction.PaymentTransactionRepository
import me.hyunlee.laundry.payment.domain.model.method.AchInfo
import me.hyunlee.laundry.payment.domain.model.method.PaymentMethodType
import me.hyunlee.laundry.payment.domain.model.method.WalletInfo
import me.hyunlee.laundry.payment.domain.model.method.WalletType
import me.hyunlee.laundry.payment.domain.model.transaction.PaymentTransaction
import me.hyunlee.laundry.payment.domain.model.transaction.PaymentTransactionStatus.*
import me.hyunlee.laundry.payment.history.outbox.PaymentEventAppender
import me.hyunlee.laundry.payment.history.outbox.PaymentEventEnvelope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Service
class PaymentTransactionCommandService(
    private val pmRepo: PaymentMethodRepository,
    private val txRepo: PaymentTransactionRepository,

    private val txPort: PaymentTransactionProviderPort,
    private val customerPort: CustomerProviderPort,
    private val idemPort: IdempotencyServicePort,

    private val appender: PaymentEventAppender,
    private val meter: MeterRegistry,
    private val slack: SlackNotifier,
) : PaymentTransactionUseCase {

    private val log = LoggerFactory.getLogger(PaymentTransactionCommandService::class.java)

    @Transactional
    override fun authorize(
        orderId: UUID,
        userId: UserId,
        expectedAmount: Long,
        currency: String,
    ): AuthorizeResult {
        val idempotentKey = "auth:$orderId"

        return idemPort.execute(
            userId = userId.value,
            key = idempotentKey,
            resourceType = "PAYMENT_AUTH",
            responseType = AuthorizeResult::class.java
        ) {

            val defaultPm = pmRepo.findDefaultByUser(userId) ?: error("No default payment method for user=${userId.value}")

            val customerId = customerPort.ensureCustomerId(userId.toString())
            val amountAuthorized = ceil10Percent(expectedAmount)

            val created = txPort.createPaymentIntentAuthorize(
                customerId = customerId,
                paymentMethod = defaultPm,
                amount = amountAuthorized,
                onSession = true,
                idempotencyKey = idempotentKey
            )

            val initialStatus = when (created.status?.lowercase()) {
                "requires_capture" -> REQUIRES_CAPTURE
                "requires_confirmation", "requires_action" -> REQUIRES_CONFIRMATION
                "processing", "requires_payment_method" -> REQUIRES_CONFIRMATION
                else -> REQUIRES_CONFIRMATION
            }

            val tx = PaymentTransaction(
                orderId = orderId,
                userId = userId,
                paymentIntentId = created.id,
                currency = currency.lowercase(),
                amountAuthorized = amountAuthorized,
                amountCaptured = 0,
                status = initialStatus,
                retryCount = 0,
                authorizedAt = OffsetDateTime.now(),
                capturedAt = null,
                canceledAt = null,
                refundedLastAt = null,
            )

            txRepo.save(tx)

            meter.counter("payment.intent.authorize.success.count", "provider", "stripe").increment()
            log.info("[Charge] Authorized init order={} pi={} amount={} {} status={}", orderId, created.id, amountAuthorized, currency, created.status)

            appender.append(
                PaymentEventEnvelope(
                    aggregateId = created.id,
                    type = when (initialStatus) {
                        REQUIRES_CAPTURE -> "REQUIRES_CAPTURE"
                        REQUIRES_CONFIRMATION -> "REQUIRES_CONFIRMATION"
                        else -> "AUTHORIZED"
                    },
                    orderId = orderId.toString(),
                    userId = userId.value.toString(),
                    provider = mapOf("pi" to created.id),
                    amount = amountAuthorized,
                    currency = currency.lowercase(),
                    actor = "OWNER",
                    idempotencyKey = idempotentKey, // 클라이언트에서 온 키 (이벤트/외부용)
                )
            )

            val result = AuthorizeResult(
                paymentIntentId = created.id,
                clientSecret = created.clientSecret,
                status = created.status,
                type = created.type,
                amountAuthorized = amountAuthorized,
                currency = currency.lowercase()
            )

            result.paymentIntentId to result
        }
    }

    @Transactional
    override fun authorizeWithPaymentMethod(
        orderId: UUID,
        userId: UserId,
        pmId: PaymentMethodId,
        expectedAmount: Long
    ): AuthorizeResult {
        // TODO userId - OrderId validation needed.
        val idempotentKey = "auth:$orderId"

        return idemPort.execute(
            userId = userId.value,
            key = idempotentKey,
            resourceType = "PAYMENT_AUTH",
            responseType = AuthorizeResult::class.java
        ) {

            val targetPm = pmRepo.findById(pmId) ?: error("Payment method not found id=${pmId.value}")

            val customerId = customerPort.ensureCustomerId(userId.toString())

            val created = txPort.createPaymentIntentAuthorize(
                customerId = customerId,
                paymentMethod = targetPm,
                amount = ceil10Percent(expectedAmount),
                onSession = true,
                idempotencyKey = idempotentKey
            )

            log.info("[TRANSACTION] intent status after created, status: ${created.status}")

            val status = when (created.status?.lowercase()) {
                "requires_capture" -> REQUIRES_CAPTURE
                "requires_confirmation", "requires_action" -> REQUIRES_CONFIRMATION
                "processing", "requires_payment_method" -> REQUIRES_CONFIRMATION
                else -> AUTHORIZED
            }

            val tx = PaymentTransaction(
                orderId = orderId,
                userId = userId,
                paymentIntentId = created.id,
                amountAuthorized = created.amount,
                amountCaptured = 0,
                status = status,
                retryCount = 0,
                authorizedAt = OffsetDateTime.now(),
                capturedAt = null,
                canceledAt = null,
                refundedLastAt = null,
            )

            txRepo.save(tx)
            meter.counter("payment.intent.authorize.success.count", "provider", "stripe").increment()
            log.info("[Charge] Authorized(init pm-specified) order={} pi={} amount={} status={}", orderId, created.id, created.amount, created.status)

            val result = AuthorizeResult(
                paymentIntentId = created.id,
                clientSecret = created.clientSecret,
                status = created.status,
                type = created.type,
                amountAuthorized = created.amount,
                currency = created.currency
            )

            result.sendEvent(
                type = status.toString(),
                paymentIntentId = created.id,
                orderId = orderId,
                userId = userId,
                amount = created.amount,
                idempotencyKey = idempotentKey
            )

            return@execute result.paymentIntentId to result
        }
    }

    /**
     * 세탁 완료 후 캡처(부분 캡처 허용)
     */
    @Transactional
    override fun capture(orderId: UUID, amount: Long?): CaptureResult {
        val tx = txRepo.findByOrderId(orderId) ?: error("Payment transaction not found for order=$orderId")
        require(tx.status == REQUIRES_CAPTURE) { "Transaction is not capturable: status=${tx.status}" }

        val amountToCapture = amount ?: tx.amountAuthorized
        require(amountToCapture <= tx.amountAuthorized) { "Capture amount exceeds authorized amount" }

        val result = txPort.capturePaymentIntent(tx.paymentIntentId, amountToCapture)

        val captured = result.amountCaptured ?: amountToCapture
        txRepo.updateCapturedByPiId(tx.paymentIntentId, captured)

        meter.counter("payment.intent.capture.success.count", "provider", "stripe").increment()
        log.info("[Charge] Captured order={} pi={} amount={} receipt={} status={} ", orderId, tx.paymentIntentId, captured, result.receiptUrl, result.status)

        appender.append(
            PaymentEventEnvelope(
                aggregateId = tx.paymentIntentId,
                type = "CAPTURED",
                orderId = orderId.toString(),
                userId = tx.userId?.value?.toString(),
                provider = mapOf("pi" to tx.paymentIntentId),
                amount = captured,
                currency = tx.currency,
                actor = "OWNER",
                payload = mapOf("receiptUrl" to result.receiptUrl)
            )
        )

        return result
    }

    /**
     * 환불 생성(전액 또는 부분). 멱등키 필수.
     * - 즉시 DB에 누적 환불액을 반영하지 않고, Stripe 웹훅의 `charge.refunded`/`charge.refund.updated`로 동기화합니다.
     */
    @Transactional
    override fun refund(orderId: UUID, amount: Long?, reason: String?, idempotentKey: String): RefundResult {
        val tx = txRepo.findByOrderId(orderId) ?: error("Payment transaction not found for order=$orderId")
        require(tx.amountCaptured > 0L) { "Refund is only available after capture" }

        val result = txPort.refundPaymentIntent(
            paymentIntentId = tx.paymentIntentId,
            amount = amount,
            reason = reason,
            idempotencyKey = idempotentKey
        )

        meter.counter("payment.refund.create.count", "provider", "stripe", "status", result.status ?: "unknown").increment()
        log.info("[Refund] Created refund for order={} pi={} refund={} amount={} status={} reason={}",
            orderId, tx.paymentIntentId, result.refundId, result.amountRefunded, result.status, reason)

        // Outbox 이벤트 발행 (REFUND_CREATED)
        appender.append(
            PaymentEventEnvelope(
                aggregateId = tx.paymentIntentId,
                type = "REFUND_CREATED",
                orderId = orderId.toString(),
                userId = tx.userId?.value?.toString(),
                provider = mapOf("pi" to tx.paymentIntentId, "ch" to result.chargeId),
                amount = result.amountRefunded,
                currency = result.currency,
                actor = "OWNER",
                reason = reason,
                idempotencyKey = idempotentKey,
                payload = mapOf("refundId" to result.refundId)
            )
        )

        return result
    }

    /**
     * 선승인 취소(캡처 전)
     */
    @Transactional
    override fun cancelAuthorization(orderId: UUID): Boolean {
        val tx = txRepo.findByOrderId(orderId) ?: error("Payment transaction not found for order=$orderId")
        require(tx.status == REQUIRES_CAPTURE || tx.status == REQUIRES_CONFIRMATION) {
            "Only uncaptured transactions can be canceled: status=${tx.status}"
        }
        val status = txPort.cancelPaymentIntent(tx.paymentIntentId)
        txRepo.updateStatusByPiId(tx.paymentIntentId, CANCELED)

        meter.counter("payment.intent.canceled.count", "provider", "stripe").increment()
        log.info("[Charge] Cancel authorization order={} pi={} status={} ", orderId, tx.paymentIntentId, status)

        appender.append(
            PaymentEventEnvelope(
                aggregateId = tx.paymentIntentId,
                type = "CANCELED",
                orderId = orderId.toString(),
                userId = tx.userId?.value?.toString(),
                provider = mapOf("pi" to tx.paymentIntentId),
                actor = "OWNER"
            )
        )
        return true
    }

    /**
     * 클라이언트 on-session confirm 이후 상태 동기화: REQUIRES_CONFIRMATION -> REQUIRES_CAPTURE 전이
     */
    @Transactional
    override fun finalizeAuthorize(orderId: UUID, paymentIntentId: String): FinalizeAuthorizeResult {
        val tx = txRepo.findByOrderId(orderId) ?: error("Payment transaction not found for order=$orderId")
        require(tx.paymentIntentId == paymentIntentId) { "PaymentIntent mismatch for order=$orderId" }

        val snapshot: RetrievedPiInfo = txPort.retrievePaymentIntent(paymentIntentId)
        val status = snapshot.status?.lowercase()

        when (status) {
            "requires_capture", "processing" -> {
                txRepo.updateStatusByPiId(paymentIntentId, REQUIRES_CAPTURE)
                meter.counter("payment.intent.finalize.success.count", "provider", "stripe").increment()
                appender.append(
                    PaymentEventEnvelope(
                        aggregateId = paymentIntentId,
                        type = "REQUIRES_CAPTURE",
                        orderId = orderId.toString(),
                        userId = tx.userId?.value?.toString(),
                        provider = mapOf("pi" to paymentIntentId),
                        amount = snapshot.amount,
                        currency = snapshot.currency,
                        actor = "OWNER"
                    )
                )
            }
            "succeeded" -> {
                txRepo.updateCapturedByPiId(paymentIntentId, snapshot.amount ?: tx.amountAuthorized)
                meter.counter("payment.intent.finalize.success.count", "provider", "stripe").increment()
                appender.append(
                    PaymentEventEnvelope(
                        aggregateId = paymentIntentId,
                        type = "CAPTURED",
                        orderId = orderId.toString(),
                        userId = tx.userId?.value?.toString(),
                        provider = mapOf("pi" to paymentIntentId),
                        amount = (snapshot.amount ?: tx.amountAuthorized),
                        currency = snapshot.currency,
                        actor = "OWNER"
                    )
                )
            }
            "requires_confirmation", "requires_action", "requires_payment_method" -> {
                meter.counter("payment.intent.finalize.failure.count", "provider", "stripe").increment()
                slack.notify(
                    title = "Payment Finalize Pending",
                    message = "PI still not capturable. status=${snapshot.status}",
                    severity = "WARN",
                    context = mapOf(
                        "orderId" to orderId.toString(),
                        "pi" to paymentIntentId
                    )
                )
            }
            else -> {
                meter.counter("payment.intent.finalize.failure.count", "provider", "stripe").increment()
                slack.notify(
                    title = "Payment Finalize Unexpected Status",
                    message = "status=${snapshot.status}",
                    severity = "ERROR",
                    context = mapOf(
                        "orderId" to orderId.toString(),
                        "pi" to paymentIntentId
                    )
                )
            }
        }

        log.info("[Charge] Finalize authorize order={} pi={} status={} capturable={} ", orderId, paymentIntentId, snapshot.status, snapshot.amountCapturable)

        return FinalizeAuthorizeResult(
            paymentIntentId = snapshot.id,
            status = snapshot.status,
            amountAuthorized = tx.amountAuthorized,
            amountCapturable = snapshot.amountCapturable,
            currency = snapshot.currency
        )
    }

    private fun ceil10Percent(amount: Long): Long {
        val safe = BigDecimal(amount)
        return safe.multiply(BigDecimal(110)).add(BigDecimal(99)).divideToIntegralValue(BigDecimal(100)).longValueExact()
    }

    private fun <R> R.sendEvent(
        type: String,
        paymentIntentId: String,
        orderId: UUID,
        userId: UserId?,
        amount: Long? = null,
        currency: String? = "usd",
        idempotencyKey: String? = null,
        reason: String? = null,
        providerExtra: Map<String, String?> = emptyMap(),
        payload: Map<String, Any?> = emptyMap(),
    ): R {

        val provider = buildMap {
            put("pi", paymentIntentId)
            providerExtra.forEach { (k, v) -> if (v != null) put(k, v) }
        }

        appender.append(
            PaymentEventEnvelope(
                aggregateId = paymentIntentId,
                type = type,
                orderId = orderId.toString(),
                userId = userId?.value?.toString(),
                provider = provider,
                amount = amount,
                currency = currency,
                actor = "OWNER",
                idempotencyKey = idempotencyKey,
                reason = reason,
                payload = payload
            )
        )

        return this
    }
}
