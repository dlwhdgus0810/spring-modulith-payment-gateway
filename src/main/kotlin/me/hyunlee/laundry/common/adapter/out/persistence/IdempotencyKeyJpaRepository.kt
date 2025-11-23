package me.hyunlee.laundry.common.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface IdempotencyKeyJpaRepository : JpaRepository<IdempotencyRecord, UUID> {

    fun findByUserIdAndIdempotencyKey(userId: UUID?, idempotencyKey: String): IdempotencyRecord?

}
