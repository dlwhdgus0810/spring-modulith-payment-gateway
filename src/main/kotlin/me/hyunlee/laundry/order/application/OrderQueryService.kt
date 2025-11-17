package me.hyunlee.laundry.order.application

import me.hyunlee.laundry.order.application.port.`in`.OrderQueryUseCase
import me.hyunlee.laundry.order.application.port.out.OrderRepository
import me.hyunlee.laundry.order.domain.exception.OrderException
import me.hyunlee.laundry.order.domain.model.Order
import me.hyunlee.laundry.order.domain.model.OrderId
import org.springframework.stereotype.Service

@Service
class OrderQueryService(
    private val repo: OrderRepository
) : OrderQueryUseCase {
    override fun getById(orderId: OrderId): Order {
        return repo.findById(orderId.value) ?: throw OrderException.OrderNotFoundException(orderId.toString());
    }
}