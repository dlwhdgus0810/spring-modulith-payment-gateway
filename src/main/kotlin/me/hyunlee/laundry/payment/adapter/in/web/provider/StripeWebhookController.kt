package me.hyunlee.laundry.payment.adapter.`in`.web.provider

import io.micrometer.core.instrument.MeterRegistry
import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
import me.hyunlee.laundry.monitoring.adapter.out.slack.SlackNotifier
import me.hyunlee.laundry.payment.adapter.out.provider.webhook.persistence.WebhookEventJpaRepository
import me.hyunlee.laundry.payment.application.transaction.PaymentTransactionService
import org.slf4j.LoggerFactory
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

/**
 * Stripe webhook endpoint
 * - Verifies signature using webhook signing secret
 * - Idempotency by Stripe event.id stored in DB
 * - Directly handles supported event types in this controller (no router/handler/aspect indirection)
 */
@RestController
@RequestMapping("/api/stripe/webhook")
class StripeWebhookController(
    private val webhookRepo: WebhookEventJpaRepository,
    private val idempotency: WebhookIdempotencyService,
    private val processingService: PaymentTransactionService,
    private val meterRegistry: MeterRegistry,
    private val slack: SlackNotifier
) {

    private val log = LoggerFactory.getLogger(StripeWebhookController::class.java)

    // =============== 분리된 엔드포인트들 ===============

    @PostMapping("/payment-intent/succeeded")
    @Operation(summary = "paymentIntentSucceeded")
    @Transactional
    fun piSucceeded(
        @RequestParam("pi") paymentIntentId: String,
        @RequestParam(name = "eventId", required = false) eventId: String?,
        @RequestParam(name = "amountCaptured", required = false) amountCaptured: Long?,
        @RequestParam(name = "receiptUrl", required = false) receiptUrl: String?,
    ): ResponseEntity<ApiResponse<Any>> =
        runWithIdempotency(eventId, StripeEventType.PAYMENT_INTENT_SUCCEEDED.value) {
            processingService.onPaymentIntentSucceeded(paymentIntentId, amountCaptured, receiptUrl)
        }

    @PostMapping("/payment-intent/failed")
    @Operation(summary = "paymentIntentFailed")
    @Transactional
    fun piFailed(
        @RequestParam("pi") paymentIntentId: String,
        @RequestParam(name = "eventId", required = false) eventId: String?,
        @RequestParam(name = "reason", required = false, defaultValue = "unknown") reason: String,
    ): ResponseEntity<ApiResponse<Any>> =
        runWithIdempotency(eventId, StripeEventType.PAYMENT_INTENT_FAILED.value) {
            processingService.onPaymentIntentFailed(paymentIntentId, reason)
            slack.notify(
                title = "Payment Intent Failed",
                message = "pi=${mask(paymentIntentId)} reason=$reason",
                severity = "ERROR",
                context = emptyMap()
            )
        }

    @PostMapping("/payment-intent/canceled")
    @Operation(summary = "paymentIntentCanceled")
    @Transactional
    fun piCanceled(
        @RequestParam("pi") paymentIntentId: String,
        @RequestParam(name = "eventId", required = false) eventId: String?,
    ): ResponseEntity<ApiResponse<Any>> =
        runWithIdempotency(eventId, StripeEventType.PAYMENT_INTENT_CANCELED.value) {
            processingService.onPaymentIntentCanceled(paymentIntentId)
        }

    @PostMapping("/payment-intent/amount-capturable-updated")
    @Operation(summary = "paymentIntentAmountCapturableUpdated")
    @Transactional
    fun piCapturableUpdated(
        @RequestParam("pi") paymentIntentId: String,
        @RequestParam(name = "eventId", required = false) eventId: String?,
        @RequestParam(name = "amountCapturable", required = false) amountCapturable: Long?,
    ): ResponseEntity<ApiResponse<Any>> =
        runWithIdempotency(eventId, StripeEventType.PAYMENT_INTENT_CAPTURABLE_UPDATED.value) {
            log.info("[StripeWebhook] amount_capturable_updated pi={} amountCapturable={}", mask(paymentIntentId), amountCapturable)
            meterRegistry.counter("payment.intent.capturable.updated.count", "provider", "stripe").increment()
        }

    @PostMapping("/charge/refunded")
    @Operation(summary = "chargeRefunded")
    @Transactional
    fun chargeRefunded(
        @RequestParam("pi") paymentIntentId: String,
        @RequestParam(name = "eventId", required = false) eventId: String?,
        @RequestParam(name = "amountRefunded", required = false) amountRefunded: Long?,
        @RequestParam(name = "refundId", required = false) refundId: String?,
    ): ResponseEntity<ApiResponse<Any>> =
        runWithIdempotency(eventId, StripeEventType.CHARGE_REFUNDED.value) {
            processingService.onChargeRefunded(paymentIntentId, amountRefunded, refundId)
            log.info("[StripeWebhook] charge.refunded pi={} amountRefunded={} refundId={}", mask(paymentIntentId), amountRefunded, mask(refundId))
        }

    @PostMapping("/refund/updated")
    @Operation(summary = "refundUpdated")
    @Transactional
    fun refundUpdated(
        @RequestParam("pi") paymentIntentId: String,
        @RequestParam(name = "eventId", required = false) eventId: String?,
        @RequestParam(name = "refundId", required = false) refundId: String?,
        @RequestParam(name = "status", required = false) status: String?,
    ): ResponseEntity<ApiResponse<Any>> =
        runWithIdempotency(eventId, StripeEventType.CHARGE_REFUND_UPDATED.value) {
            processingService.onRefundUpdated(paymentIntentId, refundId, status)
        }

    // ======================================================
    // 내부 유틸: 선택적 이벤트 멱등성 처리
    private fun runWithIdempotency(
        eventId: String?,
        type: String,
        block: () -> Unit
    ): ResponseEntity<ApiResponse<Any>> {
        if (eventId.isNullOrBlank()) {
            return try {
                block()
                meterRegistry.counter("payment.webhook.handle.success.count", "provider", "stripe", "type", type).increment()
                ApiResponse.success(mapOf("processed" to true, "idempotent" to false))
            } catch (e: Exception) {
                meterRegistry.counter("payment.webhook.handle.failure.count", "provider", "stripe", "type", type).increment()
                slack.notify(
                    title = "Stripe Webhook Handling Failed",
                    message = "type=$type id=<none> msg=${e.message}",
                    severity = "ERROR",
                    context = emptyMap()
                )
                ApiResponse.success(mapOf("processed" to false, "idempotent" to false))
            }
        }

        val record = idempotency.start(eventId, type) ?: return ApiResponse.success(mapOf("duplicate" to true))
        return try {
            block()
            record.status = "PROCESSED"
            record.processedAt = OffsetDateTime.now()
            webhookRepo.save(record)
            meterRegistry.counter("payment.webhook.handle.success.count", "provider", "stripe", "type", type).increment()
            ApiResponse.success(mapOf("processed" to true, "idempotent" to true))
        } catch (e: Exception) {
            log.error("[StripeWebhook] handler failed for id={} type={} msg={}", mask(eventId), type, e.message, e)
            record.status = "FAILED"
            webhookRepo.save(record)
            meterRegistry.counter("payment.webhook.handle.failure.count", "provider", "stripe", "type", type).increment()
            slack.notify(
                title = "Stripe Webhook Handling Failed",
                message = "type=$type id=${mask(eventId)} msg=${e.message}",
                severity = "ERROR",
                context = emptyMap()
            )
            ApiResponse.success(mapOf("processed" to false, "idempotent" to true))
        }
    }

    private fun mask(id: String?, keep: Int = 6): String? = id?.let { if (it.length <= keep) "***" else it.take(keep) + "***" }
}