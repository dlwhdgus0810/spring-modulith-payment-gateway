package me.hyunlee.laundry.order.adapter.out.persistence.order

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface OrderJpaRepository : JpaRepository<OrderEntity, UUID>