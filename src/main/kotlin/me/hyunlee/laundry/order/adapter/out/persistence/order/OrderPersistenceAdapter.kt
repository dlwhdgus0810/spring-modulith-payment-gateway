package me.hyunlee.laundry.order.adapter.out.persistence.order

import me.hyunlee.laundry.order.domain.model.Order
import me.hyunlee.laundry.order.application.port.out.OrderRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = true)
class OrderPersistenceAdapter(
    private val jpa: OrderJpaRepository
) : OrderRepository {

    @Transactional
    override fun save(order: Order): Order {
        return jpa.save(order.toEntity()).toDomain()
    }
}