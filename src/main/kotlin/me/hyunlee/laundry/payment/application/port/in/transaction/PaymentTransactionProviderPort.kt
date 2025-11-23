package me.hyunlee.laundry.payment.application.port.`in`.transaction

import me.hyunlee.laundry.payment.domain.model.method.PaymentMethod
import java.time.Instant

/**
 * 결제 트랜잭션(PaymentIntent/Refund) 전용 프로바이더 포트
 */
interface PaymentTransactionProviderPort {
    /** Create a PaymentIntent with manual capture for authorization (pre-authorization). */
    fun createPaymentIntentAuthorize(
        customerId: String,
        paymentMethod: PaymentMethod,
        amount: Long,
        onSession: Boolean,
        idempotencyKey: String? = null
    ): CreatedPiInfo

    /** Capture a previously authorized PaymentIntent. Optionally support partial capture by amount. */
    fun capturePaymentIntent(paymentIntentId: String, amount: Long? = null): CaptureResult

    /** Cancel an authorized (uncaptured) PaymentIntent. */
    fun cancelPaymentIntent(paymentIntentId: String): String

    /** Retrieve PaymentIntent snapshot (status, amounts, etc.) for finalize-authorize or diagnostics. */
    fun retrievePaymentIntent(paymentIntentId: String): RetrievedPiInfo

    /**
     * Create a refund for a captured charge derived from the given PaymentIntent.
     * - If amount is null, refunds the full captured amount.
     * - reason is optional and provider-specific.
     * - Uses idempotencyKey to prevent duplicate refunds.
     */
    fun refundPaymentIntent(
        paymentIntentId: String,
        amount: Long? = null,
        reason: String? = null,
        idempotencyKey: String? = null
    ): RefundResult

    /** Retrieve a refund by id (provider re_*). */
    fun retrieveRefund(refundId: String): RefundInfo
}

/** Common marker for lightweight projections that should record creation time. */
interface CreatedAtStamped {
    val createdAt: Instant
}

/** Lightweight projection for created PaymentIntent (authorization stage). */
data class CreatedPiInfo(
    val id: String,
    val status: String?,
    val type: String?,
    val clientSecret: String?,
    val amount: Long,
    val currency: String?,
) : CreatedAtStamped {
    override val createdAt: Instant = Instant.now()
}

/** Lightweight projection for captured result. */
data class CaptureResult(
    val id: String,
    val status: String?,
    val amountCaptured: Long?,
    val receiptUrl: String?,
) : CreatedAtStamped {
    override val createdAt: Instant = Instant.now()
}

/** Lightweight projection of PaymentIntent used for finalize-authorize and monitoring. */
data class RetrievedPiInfo(
    val id: String,
    val status: String?,
    val amount: Long?,
    val currency: String?,
    val customerId: String?,
    val paymentMethodId: String?,
    val amountCapturable: Long?,
    val latestChargeReceiptUrl: String?,
) : CreatedAtStamped {
    override val createdAt: Instant = Instant.now()
}

/** Lightweight projection for refund creation result. */
data class RefundResult(
    val refundId: String,
    val status: String?,
    val amountRefunded: Long?,
    val currency: String?,
    val chargeId: String?,
    val paymentIntentId: String?,
) : CreatedAtStamped {
    override val createdAt: Instant = Instant.now()
}

/** Lightweight projection for refund snapshot. */
data class RefundInfo(
    val refundId: String,
    val status: String?,
    val amount: Long?,
    val currency: String?,
    val chargeId: String?,
    val paymentIntentId: String?,
) : CreatedAtStamped {
    override val createdAt: Instant = Instant.now()
}

