package me.hyunlee.laundry.payment.domain.exception.method

sealed class PaymentMethodException(message: String) : RuntimeException(message) {
    class AlreadyExists(userId: String, providerPmId: String) : PaymentMethodException(
        "Payment method already exists for user=$userId with providerPmId=$providerPmId"
    )

    class NotFound(id: String) : PaymentMethodException("PaymentMethod not found: id=$id")

    class AccessDenied(userId: String, paymentMethodId: String) : PaymentMethodException(
        "PaymentMethod access denied: user=$userId, paymentMethodId=$paymentMethodId"
    )

    class SetupIntentFailed(status: String, pmId: String) : PaymentMethodException(
        "SetupIntent failed with status=$status for pm=$pmId"
    )
}