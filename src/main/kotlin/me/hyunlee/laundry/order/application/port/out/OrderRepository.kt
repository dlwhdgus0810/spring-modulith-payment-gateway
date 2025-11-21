package me.hyunlee.laundry.order.application.port.out

import me.hyunlee.laundry.order.domain.model.Order

interface OrderRepository {
    fun save(order: Order) : Order

    fun findById(id: java.util.UUID): Order?
    fun findAll(): List<Order>
}