package me.hyunlee.laundry.auth.adapter.out.persistence

import me.hyunlee.laundry.auth.application.port.out.OtpChallengeRepository
import me.hyunlee.laundry.auth.application.port.out.RefreshTokenRepository
import me.hyunlee.laundry.auth.domain.model.OtpChallenge
import me.hyunlee.laundry.auth.domain.model.OtpStatus
import me.hyunlee.laundry.auth.domain.model.RefreshToken
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Component
class OtpChallengePersistenceAdapter(
    private val jpa: OtpChallengeJpaRepository
) : OtpChallengeRepository {

    private fun toEntity(m: OtpChallenge) = OtpChallengeEntity(
        id = m.id,
        phone = m.phone,
        channel = m.channel,
        codeHash = m.codeHash,
        expiresAt = m.expiresAt,
        maxAttempts = m.maxAttempts,
        attempts = m.attempts,
        status = m.status,
    )

    private fun toModel(e: OtpChallengeEntity) = OtpChallenge(
        id = e.id,
        phone = e.phone,
        channel = e.channel,
        codeHash = e.codeHash,
        expiresAt = e.expiresAt,
        maxAttempts = e.maxAttempts,
        attempts = e.attempts,
        status = e.status,
        createdAt = e.createdAt
    )

    @Transactional
    override fun save(entity: OtpChallenge): OtpChallenge {
        val saved = jpa.save(toEntity(entity))
        return toModel(saved)
    }

    override fun findActiveByPhone(phone: String): OtpChallenge? =
        jpa.findFirstByPhoneAndStatusInOrderByCreatedAtDesc(phone, listOf(OtpStatus.PENDING))?.let { toModel(it) }

    @Transactional
    override fun markBlocked(id: UUID) {
        jpa.findById(id).ifPresent { it.status = OtpStatus.BLOCKED; jpa.save(it) }
    }

    @Transactional
    override fun markVerified(id: UUID) {
        jpa.findById(id).ifPresent { it.status = OtpStatus.VERIFIED; jpa.save(it) }
    }

    override fun incrementAttemptsIfPending(id: UUID, now: Instant): Boolean =
        jpa.incrementAttemptsIfPending(id, now) > 0

    override fun markVerifiedIfMatch(id: UUID, codeHash: String, now: Instant): Boolean =
        jpa.markVerifiedIfMatch(id, codeHash, now) > 0
}

@Component
class RefreshTokenPersistenceAdapter(
    private val jpa: RefreshTokenJpaRepository,
) : RefreshTokenRepository {

    private fun toEntity(m: RefreshToken) = RefreshTokenEntity(
        id = m.id,
        userId = m.userId,
        tokenHash = m.tokenHash,
        issuedAt = m.issuedAt,
        expiresAt = m.expiresAt,
        rotatedFromId = m.rotatedFromId,
        revokedAt = m.revokedAt,
    )

    private fun toModel(e: RefreshTokenEntity) = RefreshToken(
        id = e.id,
        userId = e.userId,
        tokenHash = e.tokenHash,
        issuedAt = e.issuedAt,
        expiresAt = e.expiresAt,
        rotatedFromId = e.rotatedFromId,
        revokedAt = e.revokedAt,
    )

    override fun save(entity: RefreshToken): RefreshToken = toModel(jpa.save(toEntity(entity)))

    override fun findByTokenHash(hash: String): RefreshToken? = jpa.findByTokenHash(hash)?.let { toModel(it) }

    @Transactional
    override fun revoke(id: UUID, now: Instant) {
        jpa.findById(id).ifPresent { it.revokedAt = now; jpa.save(it) }
    }

    @Transactional
    override fun revokeAllByUser(userId: UUID, now: Instant) {
        jpa.findAllByUserId(userId).forEach { it.revokedAt = now; jpa.save(it) }
    }
}
