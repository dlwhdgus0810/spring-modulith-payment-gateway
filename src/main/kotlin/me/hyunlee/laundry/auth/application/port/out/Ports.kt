package me.hyunlee.laundry.auth.application.port.out

import me.hyunlee.laundry.auth.domain.model.OtpChallenge
import me.hyunlee.laundry.auth.domain.model.RefreshToken
import java.time.Instant
import java.util.*

interface OtpChallengeRepository {
    fun save(entity: OtpChallenge): OtpChallenge
    fun findActiveByPhone(phone: String): OtpChallenge?
    fun markBlocked(id: UUID)
    fun markVerified(id: UUID)
    /** Atomically increment attempts if PENDING and not expired and below max. Returns true if updated. */
    fun incrementAttemptsIfPending(id: UUID, now: Instant): Boolean
    /** Atomically mark VERIFIED if code matches, PENDING, and not expired. Returns true if updated. */
    fun markVerifiedIfMatch(id: UUID, codeHash: String, now: Instant): Boolean
}

interface RefreshTokenRepository {
    fun save(entity: RefreshToken): RefreshToken
    fun findByTokenHash(hash: String): RefreshToken?
    fun revoke(id: UUID, now: Instant)
    fun revokeAllByUser(userId: UUID, now: Instant)
}

interface SmsSenderPort {
    fun sendOtp(toPhoneE164: String, code: String, expiresInSec: Long)
}

interface EmailSenderPort {
    fun sendOtp(toEmail: String, code: String, expiresInSec: Long)
}

interface OtpCodeGeneratorPort {
    fun generate(length: Int = 6): String
}

interface ClockPort {
    fun now(): Instant
}

interface RateLimiterPort {
    fun checkAndConsume(key: String, tokens: Int = 1)
}

interface TokenCodecPort {
    fun newAccessToken(userId: UUID, ttlSeconds: Long, claims: Map<String, Any> = emptyMap()): String
    fun newRefreshToken(): String
    fun hash(token: String): String
}
