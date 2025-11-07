package me.hyunlee.laundry.order.adapter.`in`.rest.dto

import me.hyunlee.laundry.common.PaymentMethodId
import me.hyunlee.laundry.common.UserId
import me.hyunlee.laundry.order.domain.model.Contact
import me.hyunlee.laundry.order.domain.model.OrderItem
import me.hyunlee.laundry.order.domain.model.catalog.AddOnType
import me.hyunlee.laundry.order.domain.model.catalog.ServiceType
import me.hyunlee.laundry.order.domain.port.`in`.command.CreateOrderCommand
import me.hyunlee.order.domain.model.enums.TipSelection
import me.hyunlee.order.domain.model.vo.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class CreateOrderRequest(
    val userId: UUID,
    val pmId: UUID,

    val phone: String,
    val email: String?,
    val address: AddressRequest,

    val pickupDate: String,
    val pickupTime: Int,
    val deliveryDate: String?,                // 후지정 가능
    val deliveryTime: Int?,

    val bagCount: Int,
    val items: List<OrderItemRequest>,
    val tipCents: Long?,                        // tip 없으면 null

    val idempotentKey: String?                  // client에서 생성한 key
) {
    fun toCommand(): CreateOrderCommand {
        return CreateOrderCommand(
            userId = UserId(userId),
            pmId = PaymentMethodId(pmId),
            contact = Contact(phone = phone, email = email, address = address.toDomain()),
            schedule = Schedule(
                pickupDate = isoDateParse(pickupDate),
                pickupSlot = SlotIndex(pickupTime),
                deliveryDate = deliveryDate?.let { isoDateParse(it) },
                deliverySlot = deliveryTime?.let { SlotIndex(it) }
            ),
            bagCount = BagCount(bagCount),
            items = items.map { it.toDomain() },
            tip = tipCents?.let { Tip(TipSelection.CUSTOM, BigDecimal.valueOf(tipCents)) },
            idempotentKey = idempotentKey
        )
    }

    private fun isoDateParse(isoDate : String): LocalDate {
        return LocalDate.parse(isoDate, DateTimeFormatter.ISO_DATE)
    }
}

data class AddressRequest(
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val secondary: String?,
    val instructions: String?
) {
    fun toDomain(): Address =
        Address(street, city, state, postalCode, secondary, instructions)
}

data class OrderItemRequest(
    val serviceType: String,
    val addOns: Set<String> = emptySet(),
) {
    fun toDomain(): OrderItem =
        OrderItem(serviceType = ServiceType.valueOf(serviceType), addOns = addOns.map { AddOnType.valueOf(it) }.toMutableSet())
}