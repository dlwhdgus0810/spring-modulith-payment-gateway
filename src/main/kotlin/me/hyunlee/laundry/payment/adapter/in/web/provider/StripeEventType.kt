package me.hyunlee.laundry.payment.adapter.`in`.web.provider

/**
 * Stripe Webhook event type constants (single source of truth)
 */
enum class StripeEventType(val value: String) {
    SETUP_INTENT_SUCCEEDED("setup_intent.succeeded"),
    PAYMENT_INTENT_SUCCEEDED("payment_intent.succeeded"),
    PAYMENT_INTENT_FAILED("payment_intent.payment_failed"),
    PAYMENT_INTENT_CANCELED("payment_intent.canceled"),
    PAYMENT_INTENT_CAPTURABLE_UPDATED("payment_intent.amount_capturable_updated"),
    PM_AUTOMATICALLY_UPDATED("payment_method.automatically_updated"),
    PM_DETACHED("payment_method.detached"),
    CHARGE_REFUNDED("charge.refunded"),
    CHARGE_REFUND_UPDATED("charge.refund.updated");

    companion object {
        fun of(value: String?): StripeEventType? = value?.let { v -> entries.find { it.value == v } }
    }
}
