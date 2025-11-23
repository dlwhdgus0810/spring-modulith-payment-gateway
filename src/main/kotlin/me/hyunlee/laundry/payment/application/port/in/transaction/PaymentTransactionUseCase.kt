package me.hyunlee.laundry.payment.application.port.`in`.transaction

import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import java.util.*

interface PaymentTransactionUseCase {
    fun authorize(orderId: UUID, userId: UserId, expectedAmount: Long, currency: String): AuthorizeResult
    fun authorizeWithPaymentMethod(orderId: UUID, userId: UserId, pmId: PaymentMethodId, expectedAmount: Long): AuthorizeResult

    fun capture(orderId: UUID, amount: Long?): CaptureResult
    fun cancelAuthorization(orderId: UUID): Boolean
    fun finalizeAuthorize(orderId: UUID, paymentIntentId: String): FinalizeAuthorizeResult
    fun refund(orderId: UUID, amount: Long?, reason: String?, idempotentKey: String): RefundResult
}

data class AuthorizeResult(
    val paymentIntentId: String,
    val clientSecret: String?,
    val status: String?,
    val type: String?,
    val amountAuthorized: Long,
    val currency: String?,
)

data class FinalizeAuthorizeResult(
    val paymentIntentId: String,
    val status: String?,
    val amountAuthorized: Long,
    val amountCapturable: Long?,
    val currency: String?
)
