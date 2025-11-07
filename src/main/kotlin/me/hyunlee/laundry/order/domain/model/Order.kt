package me.hyunlee.laundry.order.domain.model

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.order.domain.event.OrderCreatedEvent
import me.hyunlee.laundry.order.domain.model.vo.PaymentInfo
import me.hyunlee.order.domain.model.enums.OrderStatus
import me.hyunlee.order.domain.model.vo.Address
import me.hyunlee.order.domain.model.vo.BagCount
import me.hyunlee.order.domain.model.vo.Schedule
import me.hyunlee.order.domain.model.vo.Tip
import java.time.Instant
import java.util.*

@JvmInline
value class OrderId(val value: UUID) {
    override fun toString() = value.toString()
    companion object { fun newId(): OrderId = OrderId(UUID.randomUUID()) }
}

data class Order(
    val id: OrderId = OrderId.newId(),
    val userId: UserId,
    val payment: PaymentInfo,

    val contact: Contact,
    val schedule: Schedule,
    val bagCount: BagCount,
    val items: List<OrderItem>,
    val tip: Tip?,                       // 없을 수 있음

    val status: OrderStatus,

    val idempotentKey: String?,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {

    companion object {
        fun create(
            userId: UserId,
            paymentInfo: PaymentInfo,
            contact: Contact,
            schedule: Schedule,
            bagCount: BagCount,
            items: List<OrderItem>,
            tip: Tip?,
            idempotentKey: String?
        ): Pair<Order, OrderCreatedEvent> {
            val order = Order(
                userId = userId,
                payment = paymentInfo,
                contact = contact,
                schedule = schedule,
                bagCount = bagCount,
                items = items,
                tip = tip,
                status = OrderStatus.CREATED,
                idempotentKey = idempotentKey,
            )

            val event = OrderCreatedEvent(
                orderId = order.id.toString(),
                userId = order.userId.toString(),
            )

            return order to event
        }
    }
}

data class Contact(
    val phone: String,
    val email: String?,
    val address: Address
) {
    companion object {
        fun create(phone: String, email: String?, address: Address) =
            Contact(phone, email, address)
    }
}

