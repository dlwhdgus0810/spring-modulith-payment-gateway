package me.hyunlee.laundry.payment.application.port.`in`.customer

/**
 * 결제 Provider의 Customer 보장 전용 포트
 */
interface CustomerProviderPort {
    /** Ensure customer exists for the given domain user id string, and return cus_* id. */
    fun ensureCustomerId(userId: String): String
}
