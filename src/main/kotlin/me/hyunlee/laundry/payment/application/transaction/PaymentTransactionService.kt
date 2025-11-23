package me.hyunlee.laundry.payment.application.transaction

import me.hyunlee.laundry.payment.history.outbox.PaymentEventAppender
import me.hyunlee.laundry.payment.history.outbox.PaymentEventEnvelope
import me.hyunlee.laundry.payment.application.port.out.transaction.PaymentTransactionRepository
import me.hyunlee.laundry.payment.domain.model.transaction.PaymentTransactionStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 결제 처리(payment_intent.*) 전용 애플리케이션 서비스
 * - 결제수단 등록/스냅샷과 분리된 실제 과금 트랜잭션 관리
 */
@Service
class PaymentTransactionService(
    private val txRepo: PaymentTransactionRepository,
    private val appender: PaymentEventAppender,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun onPaymentIntentSucceeded(paymentIntentId: String, amountCaptured: Long? = null, receiptUrl: String? = null) {
        if (amountCaptured != null) {
            txRepo.updateCapturedByPiId(paymentIntentId, amountCaptured, PaymentTransactionStatus.CAPTURED)
        } else {
            txRepo.updateStatusByPiId(paymentIntentId, PaymentTransactionStatus.CAPTURED)
        }
        log.info("[Processing] payment_intent.succeeded pi={} captured={} receiptUrl={}", paymentIntentId, amountCaptured, receiptUrl)

        // Outbox 이벤트 (WEBHOOK)
        val tx = txRepo.findByPaymentIntentId(paymentIntentId)
        appender.append(
            PaymentEventEnvelope(
                aggregateId = paymentIntentId,
                type = "CAPTURED",
                orderId = tx?.orderId?.toString(),
                userId = tx?.userId?.value?.toString(),
                provider = mapOf("pi" to paymentIntentId),
                amount = (amountCaptured ?: tx?.amountCaptured),
                currency = tx?.currency,
                actor = "WEBHOOK",
                payload = mapOf("receiptUrl" to receiptUrl)
            )
        )
    }

    @Transactional
    fun onPaymentIntentFailed(paymentIntentId: String, reason: String) {
        txRepo.updateStatusByPiId(paymentIntentId, PaymentTransactionStatus.FAILED)
        log.warn("[Processing] payment_intent.payment_failed pi={} reason={}", paymentIntentId, reason)

        val tx = txRepo.findByPaymentIntentId(paymentIntentId)
        appender.append(
            PaymentEventEnvelope(
                aggregateId = paymentIntentId,
                type = "FAILED",
                orderId = tx?.orderId?.toString(),
                userId = tx?.userId?.value?.toString(),
                provider = mapOf("pi" to paymentIntentId),
                actor = "WEBHOOK",
                reason = reason
            )
        )
    }

    @Transactional
    fun onPaymentIntentCanceled(paymentIntentId: String) {
        txRepo.updateStatusByPiId(paymentIntentId, PaymentTransactionStatus.CANCELED)
        log.info("[Processing] payment_intent.canceled pi={}", paymentIntentId)

        val tx = txRepo.findByPaymentIntentId(paymentIntentId)
        appender.append(
            PaymentEventEnvelope(
                aggregateId = paymentIntentId,
                type = "CANCELED",
                orderId = tx?.orderId?.toString(),
                userId = tx?.userId?.value?.toString(),
                provider = mapOf("pi" to paymentIntentId),
                actor = "WEBHOOK"
            )
        )
    }

    /** charge.refunded에서 누적 환불액(amount_refunded) 반영 */
    @Transactional
    fun onChargeRefunded(paymentIntentId: String, amountRefunded: Long?, lastRefundId: String? = null) {
        val refunded = amountRefunded ?: return
        txRepo.updateRefundedByPiId(paymentIntentId, refunded)
        log.info("[Processing] charge.refunded pi={} amountRefunded={}", paymentIntentId, refunded)

        val tx = txRepo.findByPaymentIntentId(paymentIntentId)
        appender.append(
            PaymentEventEnvelope(
                aggregateId = paymentIntentId,
                type = "REFUND_UPDATED",
                orderId = tx?.orderId?.toString(),
                userId = tx?.userId?.value?.toString(),
                provider = mapOf("pi" to paymentIntentId),
                amount = refunded,
                currency = tx?.currency,
                actor = "WEBHOOK",
                payload = mapOf("refundId" to lastRefundId)
            )
        )
    }

    /** charge.refund.updated에서 최신 refund 상태 메모 */
    @Transactional
    fun onRefundUpdated(paymentIntentId: String, refundId: String?, status: String?) {
        // RDB에는 환불 누적액만 보관하며, 개별 refundId/status는 Mongo 이벤트 히스토리로 남깁니다.
        val exists = txRepo.findByPaymentIntentId(paymentIntentId)
        if (exists == null) {
            log.warn("[Processing] refund.updated skip: tx not found pi={} refund={} status={}", paymentIntentId, refundId, status)
            return
        }
        // 누적액 정보가 없는 이벤트이므로 RDB 업데이트는 없음. 로그만 남김.
        log.info("[Processing] refund.updated pi={} refund={} status={}", paymentIntentId, refundId, status)

        appender.append(
            PaymentEventEnvelope(
                aggregateId = paymentIntentId,
                type = "REFUND_UPDATED",
                orderId = exists.orderId?.toString(),
                userId = exists.userId?.value?.toString(),
                provider = mapOf("pi" to paymentIntentId),
                actor = "WEBHOOK",
                payload = mapOf("refundId" to refundId, "status" to status)
            )
        )
    }
}