package me.hyunlee.laundry.payment.adapter.out.outbox.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface OutboxEventJpaRepository : JpaRepository<OutboxEventEntity, UUID> {
    fun findTop100ByStatusOrderByCreatedAtAsc(status: OutboxStatus): List<OutboxEventEntity>
}
