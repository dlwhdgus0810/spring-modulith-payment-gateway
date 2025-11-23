package me.hyunlee.laundry.payment.history.outbox

import me.hyunlee.laundry.payment.adapter.out.outbox.OutboxWriter
import org.springframework.stereotype.Component

interface PaymentEventAppender {
    fun append(evt: PaymentEventEnvelope)
}

@Component
class OutboxEventAppender(
    private val outbox: OutboxWriter
) : PaymentEventAppender {
    override fun append(evt: PaymentEventEnvelope) {
        val payload = mutableMapOf<String, Any?>().apply {
            put("orderId", evt.orderId)
            put("userId", evt.userId)
            put("pi", evt.provider["pi"]) 
            put("ch", evt.provider["ch"]) 
            put("pm", evt.provider["pm"]) 
            put("cus", evt.provider["cus"]) 
            put("amount", evt.amount)
            put("currency", evt.currency)
            put("actor", evt.actor)
            put("reason", evt.reason)
            put("idempotencyKey", evt.idempotencyKey)
            put("webhookEventId", evt.webhookEventId)
            putAll(evt.payload)
        }

        outbox.append(
            aggregateType = evt.aggregateType,
            aggregateId = evt.aggregateId,
            type = evt.type,
            payload = payload
        )
    }
}
