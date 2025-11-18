package me.hyunlee.laundry.auth.domain.model

import java.time.Instant
import java.util.*

enum class OtpChannel { SMS, EMAIL }

enum class OtpStatus { PENDING, VERIFIED, EXPIRED, BLOCKED }

data class OtpChallenge(
    val id: UUID,
    val phone: String,
    val channel: OtpChannel,
    val codeHash: String,
    val expiresAt: Instant,
    val maxAttempts: Int,
    val attempts: Int,
    val status: OtpStatus,
    val createdAt: Instant,
) {
    fun isExpired(now: Instant): Boolean = now.isAfter(expiresAt)
}

data class RefreshToken(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val rotatedFromId: UUID? = null,
    val revokedAt: Instant? = null,
)
