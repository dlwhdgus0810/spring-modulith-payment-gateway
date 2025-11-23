package me.hyunlee.laundry.payment.adapter.out.transaction.persistence

import jakarta.persistence.*
import me.hyunlee.laundry.common.adapter.out.persistence.BaseEntity
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.payment.domain.model.transaction.PaymentTransaction
import me.hyunlee.laundry.payment.domain.model.transaction.PaymentTransactionStatus
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(
    name = "payment_transactions",
    indexes = [
        Index(name = "ix_payment_tx_order", columnList = "order_id"),
        Index(name = "ux_payment_tx_order", columnList = "order_id", unique = true),
        Index(name = "ux_payment_tx_pi", columnList = "payment_intent_id", unique = true),
        Index(name = "ix_payment_tx_status", columnList = "status")
    ]
)
class PaymentTransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    var id: UUID? = null,

    @Column(name = "order_id", columnDefinition = "BINARY(16)")
    var orderId: UUID? = null,

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    var userId: UUID? = null,

    // 최소 보관: paymentIntentId만 RDB 유지
    @Column(name = "payment_intent_id", length = 64, nullable = false)
    var paymentIntentId: String,

    @Column(name = "currency", length = 8, nullable = false)
    var currency: String,

    @Column(name = "amount_authorized", nullable = false)
    var amountAuthorized: Long = 0,

    @Column(name = "amount_captured", nullable = false)
    var amountCaptured: Long = 0,

    @Column(name = "amount_refunded", nullable = false)
    var amountRefunded: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    var status: PaymentTransactionStatus = PaymentTransactionStatus.REQUIRES_CONFIRMATION,

    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0,

    // receiptUrl / failureInfo / lastRefund* 등은 Mongo 이벤트로 이전함
    // expiresAt은 Provider별 의미가 달라 Mongo 이벤트 히스토리로 이동

    // 전이 시점 타임스탬프(옵션)
    @Column(name = "authorized_at")
    var authorizedAt: OffsetDateTime? = null,

    @Column(name = "captured_at")
    var capturedAt: OffsetDateTime? = null,

    @Column(name = "canceled_at")
    var canceledAt: OffsetDateTime? = null,

    @Column(name = "refunded_last_at")
    var refundedLastAt: OffsetDateTime? = null,
): BaseEntity()

fun PaymentTransactionEntity.toDomain(): PaymentTransaction = PaymentTransaction(
    id = this.id,
    orderId = this.orderId,
    userId = this.userId?.let { UserId(it) },
    paymentIntentId = this.paymentIntentId,
    currency = this.currency,
    amountAuthorized = this.amountAuthorized,
    amountCaptured = this.amountCaptured,
    amountRefunded = this.amountRefunded,
    status = this.status,
    retryCount = this.retryCount,
    authorizedAt = this.authorizedAt,
    capturedAt = this.capturedAt,
    canceledAt = this.canceledAt,
    refundedLastAt = this.refundedLastAt,
    createdAt = null,
    updatedAt = null,
)

fun PaymentTransaction.toEntity(): PaymentTransactionEntity = PaymentTransactionEntity(
    id = this.id,
    orderId = this.orderId,
    userId = this.userId?.value,
    paymentIntentId = this.paymentIntentId,
    currency = this.currency,
    amountAuthorized = this.amountAuthorized,
    amountCaptured = this.amountCaptured,
    amountRefunded = this.amountRefunded,
    status = this.status,
    retryCount = this.retryCount,
    authorizedAt = this.authorizedAt,
    capturedAt = this.capturedAt,
    canceledAt = this.canceledAt,
    refundedLastAt = this.refundedLastAt,
)
