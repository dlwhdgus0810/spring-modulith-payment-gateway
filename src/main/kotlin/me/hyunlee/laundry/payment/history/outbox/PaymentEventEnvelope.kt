package me.hyunlee.laundry.payment.history.outbox

/**
 * 결제 관련 Outbox 이벤트 표준 엔벨로프
 */
data class PaymentEventEnvelope(
    val aggregateType: String = "PaymentTransaction",
    val aggregateId: String,
    val type: String,
    val orderId: String? = null,
    val userId: String? = null,
    val provider: Map<String, String?> = emptyMap(), // keys: pi, ch, pm, cus
    val amount: Long? = null,
    val currency: String? = null,
    val actor: String? = null, // OWNER | SYSTEM | WEBHOOK
    val reason: String? = null,
    val idempotencyKey: String? = null,
    val webhookEventId: String? = null,
    val payload: Map<String, Any?> = emptyMap(),
)
