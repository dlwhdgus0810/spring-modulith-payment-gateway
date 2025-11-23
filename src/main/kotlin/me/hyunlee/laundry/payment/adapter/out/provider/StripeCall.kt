package me.hyunlee.laundry.payment.adapter.out.provider

import com.stripe.exception.StripeException
import me.hyunlee.laundry.payment.domain.exception.PaymentException
import org.slf4j.Logger

inline fun <T> stripeCall(
    log: Logger,
    operation: String? = null,
    block: () -> T
): T {
    return try {
        block()
    } catch (e: StripeException) {
        val op = operation
            ?: e.stackTrace.getOrNull(0)?.let { "${it.className}.${it.methodName}" }
            ?: "UnknownStripeOperation"

        log.error(
            "[Stripe] $op failed: code=${e.code} req=${e.requestId} msg=${e.message}",
            e
        )

        throw PaymentException.StripeError(
            operation = op,
            code = e.code,
            requestId = e.requestId,
            message = e.message ?: "Stripe error",
            cause = e
        )
    }
}