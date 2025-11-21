package me.hyunlee.laundry.order.application.port.`in`

import me.hyunlee.laundry.order.domain.model.Order
import me.hyunlee.laundry.order.domain.model.OrderId

interface OrderQueryUseCase {
    fun getById(orderId: OrderId): Order
    fun getAll(): List<Order>
}