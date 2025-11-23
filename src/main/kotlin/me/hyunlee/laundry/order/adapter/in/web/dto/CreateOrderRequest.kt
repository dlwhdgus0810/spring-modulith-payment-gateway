package me.hyunlee.laundry.order.adapter.`in`.web.dto

import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.order.application.port.`in`.command.CreateOrderCommand
import me.hyunlee.laundry.order.domain.model.OrderItem
import me.hyunlee.laundry.order.domain.model.catalog.AddOnType
import me.hyunlee.laundry.order.domain.model.catalog.ServiceType
import me.hyunlee.order.domain.model.enums.TipSelection
import me.hyunlee.order.domain.model.vo.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class CreateOrderRequest(
    val pmId: UUID,

    val address: AddressRequest,

    val pickupDate: String,
    val pickupTime: Int,
    val deliveryDate: String?,                // 후지정 가능
    val deliveryTime: Int?,

    val bagCount: Int,
    val items: List<OrderItemRequest>,
    val tip: TipRequest,
) {
    fun toCommand(currentUserId: UserId): CreateOrderCommand {
        return CreateOrderCommand(
            userId = currentUserId,
            pmId = PaymentMethodId(pmId),
            address = address.toDomain(),
            schedule = Schedule(
                pickupDate = isoDateParse(pickupDate),
                pickupSlot = SlotIndex(pickupTime),
                deliveryDate = deliveryDate?.let { isoDateParse(it) },
                deliverySlot = deliveryTime?.let { SlotIndex(it) }
            ),
            bagCount = BagCount(bagCount),
            items = items.map { it.toDomain() },
            tip = buildTipOrNull(),
        )
    }

    private fun isoDateParse(isoDate : String): LocalDate {
        return LocalDate.parse(isoDate, DateTimeFormatter.ISO_DATE)
    }

    private fun buildTipOrNull(): Tip? {
        val req = tip
        return when (req.selection) {
            TipSelection.PCT_10, TipSelection.PCT_15, TipSelection.PCT_20 -> {
                require(req.tipCents == 0L) { "tipCents must be 0 when tip.selection is percentage" }
                Tip(req.selection, null)
            }
            TipSelection.CUSTOM -> {
                require(req.tipCents >= 0) { "tipCents must be >= 0" }
                if (req.tipCents == 0L) {
                    null
                } else {
                    Tip(TipSelection.CUSTOM, BigDecimal.valueOf(req.tipCents))
                }
            }
        }
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

data class TipRequest(
    val selection: TipSelection,
    val tipCents: Long,
)