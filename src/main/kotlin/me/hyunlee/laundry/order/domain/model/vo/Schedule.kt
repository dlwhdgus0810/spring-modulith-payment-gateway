package me.hyunlee.order.domain.model.vo

import java.time.LocalDate

@JvmInline
value class SlotIndex(val value: Int) {
    init { require(value in 1..20) }
}

data class Schedule(
    val pickupDate: LocalDate, val pickupSlot: SlotIndex,
    val deliveryDate: LocalDate?, val deliverySlot: SlotIndex?    // 후지정 → null 허용
) {
    init {
        if (deliveryDate != null && deliverySlot != null) {
            require(!(deliveryDate.isBefore(pickupDate) || (deliveryDate.equals(pickupDate) && deliverySlot.value < pickupSlot.value)))
            {
                "delivery must be >= pickup"
            }
        }

        require((deliveryDate == null) == (deliverySlot == null)) {
            "deliveryDate and deliverySlot must be both null or both non-null"
        }
    }
}