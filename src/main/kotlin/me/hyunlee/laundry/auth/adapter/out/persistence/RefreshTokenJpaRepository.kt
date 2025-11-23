package me.hyunlee.laundry.auth.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByTokenHash(tokenHash: String): RefreshTokenEntity?
    fun findAllByUserId(userId: UUID): List<RefreshTokenEntity>
}
