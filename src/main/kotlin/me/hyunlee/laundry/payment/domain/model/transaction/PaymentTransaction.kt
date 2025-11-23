package me.hyunlee.laundry.payment.domain.model.transaction

import me.hyunlee.laundry.common.domain.UserId
import java.time.OffsetDateTime
import java.util.*

/**
 * 결제 처리(선승인/캡처/취소/실패 등) 전체 수명을 아우르는 영속 도메인 객체
 * - 주문과 결제수단 등록 로직과 분리된 "실제 과금" 트랜잭션 단위
 */
data class PaymentTransaction(
    val id: UUID? = null,
    val orderId: UUID?,
    val userId: UserId?,
    val paymentIntentId: String,
    val currency: String = "usd",
    val amountAuthorized: Long = 0,
    val amountCaptured: Long = 0,
    val amountRefunded: Long = 0,
    val status: PaymentTransactionStatus,
    val retryCount: Int = 0,
    val authorizedAt: OffsetDateTime? = null,
    val capturedAt: OffsetDateTime? = null,
    val canceledAt: OffsetDateTime? = null,
    val refundedLastAt: OffsetDateTime? = null,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)

enum class PaymentTransactionStatus {
    REQUIRES_CONFIRMATION,
    AUTHORIZED,
    REQUIRES_CAPTURE,
    CAPTURED,
    CANCELED,
    FAILED,
    EXPIRED
}
