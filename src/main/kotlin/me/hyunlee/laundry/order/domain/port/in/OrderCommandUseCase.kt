package me.hyunlee.laundry.order.domain.port.`in`

import me.hyunlee.laundry.order.domain.model.Order
import me.hyunlee.laundry.order.domain.port.`in`.command.CreateOrderCommand

interface OrderCommandUseCase {
    fun create(command : CreateOrderCommand) : Order
}