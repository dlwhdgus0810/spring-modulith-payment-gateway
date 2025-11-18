package me.hyunlee.laundry.auth.adapter.out.persistence

import me.hyunlee.laundry.auth.domain.model.OtpStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.*

interface OtpChallengeJpaRepository : JpaRepository<OtpChallengeEntity, UUID> {
    fun findFirstByPhoneAndStatusInOrderByCreatedAtDesc(phone: String, statuses: List<OtpStatus>): OtpChallengeEntity?

    @Modifying
    @Query(
        "update OtpChallengeEntity e set e.attempts = e.attempts + 1 " +
            "where e.id = :id and e.status = me.hyunlee.laundry.auth.domain.model.OtpStatus.PENDING " +
            "and e.expiresAt > :now and e.attempts < e.maxAttempts"
    )
    fun incrementAttemptsIfPending(
        @Param("id") id: UUID,
        @Param("now") now: Instant
    ): Int

    @Modifying
    @Query(
        "update OtpChallengeEntity e set e.status = me.hyunlee.laundry.auth.domain.model.OtpStatus.VERIFIED " +
            "where e.id = :id and e.status = me.hyunlee.laundry.auth.domain.model.OtpStatus.PENDING and e.expiresAt > :now and e.codeHash = :codeHash"
    )
    fun markVerifiedIfMatch(
        @Param("id") id: UUID,
        @Param("codeHash") codeHash: String,
        @Param("now") now: Instant
    ): Int
}
