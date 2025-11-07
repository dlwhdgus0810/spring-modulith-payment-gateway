package me.hyunlee.laundry.order.adapter.out.persistence.orderitem

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface OrderItemJpaRepository : JpaRepository<OrderItemEntity, UUID>