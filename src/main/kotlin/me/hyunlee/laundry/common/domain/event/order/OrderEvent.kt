package me.hyunlee.laundry.common.domain.event.order

import java.time.Instant
import java.util.*

interface OrderEvent {
    val occurredAt: Instant
    val aggregateType: String
    val aggregateId: String
    val eventType: String
    val eventId: String
}

data class OrderCreatedEvent(
    val orderId: String,
    val userId: String,
    val pmId: String,
    override val occurredAt: Instant = Instant.now(),
    override val eventId: String = UUID.randomUUID().toString(),
) : OrderEvent {
    override val aggregateType = "order"
    override val aggregateId = orderId
    override val eventType = "OrderCreated"
}