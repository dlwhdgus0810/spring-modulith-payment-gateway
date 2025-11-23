package me.hyunlee.laundry.payment.domain.exception

sealed class PaymentException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    class StripeError(
        val operation: String,
        val code: String?,
        val requestId: String?,
        message: String,
        cause: Throwable
    ) : PaymentException(message, cause)
}

sealed class PaymentMethodException(message: String) : PaymentException(message) {
    class AlreadyExists(userId: String, providerPmId: String) :
        PaymentMethodException("Payment method already exists for user=$userId with providerPmId=$providerPmId")

    class NotFound(id: String) :
        PaymentMethodException("PaymentMethod not found: id=$id")

    class AccessDenied(userId: String, paymentMethodId: String) :
        PaymentMethodException("PaymentMethod access denied: user=$userId, paymentMethodId=$paymentMethodId")

    class SetupIntentFailed(status: String, pmId: String) :
        PaymentMethodException("SetupIntent failed with status=$status for pm=$pmId")
}

sealed class PaymentTransactionException(message: String) : PaymentException(message) {

    class NotCapturable(status: String) :
        PaymentTransactionException("Transaction is not capturable. status=$status")

    class CaptureAmountExceedsAuthorized(requested: Long, authorized: Long) :
        PaymentTransactionException("Capture amount exceeds authorized. requested=$requested, authorized=$authorized")

    class NotFoundByOrder(orderId: String) :
        PaymentTransactionException("Payment transaction not found for order=$orderId")
}