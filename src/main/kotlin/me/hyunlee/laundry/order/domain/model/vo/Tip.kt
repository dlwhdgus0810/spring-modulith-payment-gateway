package me.hyunlee.order.domain.model.vo

import me.hyunlee.order.domain.model.enums.TipSelection
import java.math.BigDecimal

data class Tip(
    val type: TipSelection,
    val amount: BigDecimal? = null
) {
    init {
        when(type) {
            TipSelection.PCT_10,
            TipSelection.PCT_15,
            TipSelection.PCT_20 -> {
                require(amount == null) { "Percent tip must not provide custom amount" }
            }
            TipSelection.CUSTOM -> {
                require(amount != null) { "Custom tip must contain value" }
                require(amount.scale() <= 2) { "Custom amount must be 2 decimal precision" }
                require(amount >= BigDecimal.ZERO) { "Custom amount must be positive or zero" }
            }
        }
    }

    fun percentValue(): Int? =
        when(type) {
            TipSelection.PCT_10 -> 10
            TipSelection.PCT_15 -> 15
            TipSelection.PCT_20 -> 20
            TipSelection.CUSTOM -> null
        }
}