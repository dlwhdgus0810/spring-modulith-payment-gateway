package me.hyunlee.laundry.order.domain.port.out

import me.hyunlee.laundry.order.domain.model.Order

interface OrderRepository {
    fun save(order: Order) : Order
}