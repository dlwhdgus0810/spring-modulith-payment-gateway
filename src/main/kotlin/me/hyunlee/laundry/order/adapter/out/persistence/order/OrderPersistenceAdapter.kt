package me.hyunlee.laundry.order.adapter.out.persistence.order

import me.hyunlee.laundry.order.application.port.out.OrderRepository
import me.hyunlee.laundry.order.domain.model.Order
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

    override fun findById(id: java.util.UUID): Order? {
        return jpa.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findAll(): List<Order> {
        return jpa.findAll().map { it.toDomain() }
    }

}