package me.hyunlee.laundry.order.application.port.`in`

import me.hyunlee.laundry.order.domain.model.Order
import me.hyunlee.laundry.order.application.port.`in`.command.CreateOrderCommand

interface OrderCommandUseCase {
    fun create(command : CreateOrderCommand) : Order
}