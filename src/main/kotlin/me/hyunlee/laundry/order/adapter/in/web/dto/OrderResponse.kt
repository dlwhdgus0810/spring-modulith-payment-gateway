package me.hyunlee.laundry.order.adapter.`in`.web.dto

import me.hyunlee.laundry.order.domain.model.Order
import me.hyunlee.laundry.order.domain.model.OrderItem
import me.hyunlee.order.domain.model.vo.Schedule

data class OrderResponse(
    val id: String,
    val status: String,
    val contact: ContactResponse,
    val schedule: ScheduleResponse,
    val bagCount: Int,
    val items: List<OrderItemResponse>,
    val createdAt: String,
    val updatedAt: String,
)

data class ContactResponse(
    val phone: String,
    val email: String?
)

data class ScheduleResponse(
    val pickupDate: String,
    val pickupSlot: Int,
    val deliveryDate: String?,
    val deliverySlot: Int?
)

data class OrderItemResponse(
    val serviceType: String,
    val addOns: List<String>
)

fun Order.toResponse(): OrderResponse = OrderResponse(
    id = this.id.toString(),
    status = this.status.name,
    contact = ContactResponse(
        phone = this.contact.phone,
        email = this.contact.email
    ),
    schedule = this.schedule.toResponse(),
    bagCount = this.bagCount.value,
    items = this.items.map { it.toResponse() },
    createdAt = this.createdAt.toString(),
    updatedAt = this.updatedAt.toString(),
)

private fun Schedule.toResponse(): ScheduleResponse = ScheduleResponse(
    pickupDate = this.pickupDate.toString(), // ISO-8601 YYYY-MM-DD
    pickupSlot = this.pickupSlot.value,
    deliveryDate = this.deliveryDate?.toString(),
    deliverySlot = this.deliverySlot?.value
)

private fun OrderItem.toResponse(): OrderItemResponse = OrderItemResponse(
    serviceType = this.serviceType.name,
    addOns = this.addOns.map { it.name }
)
