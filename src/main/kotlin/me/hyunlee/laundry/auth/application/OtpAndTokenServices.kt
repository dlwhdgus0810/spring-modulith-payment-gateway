package me.hyunlee.laundry.auth.application

import me.hyunlee.laundry.auth.application.port.`in`.*
import me.hyunlee.laundry.auth.application.port.out.*
import me.hyunlee.laundry.auth.domain.exception.AuthException
import me.hyunlee.laundry.auth.domain.model.OtpChallenge
import me.hyunlee.laundry.auth.domain.model.OtpChannel
import me.hyunlee.laundry.auth.domain.model.OtpChannel.EMAIL
import me.hyunlee.laundry.auth.domain.model.OtpChannel.SMS
import me.hyunlee.laundry.auth.domain.model.OtpStatus.PENDING
import me.hyunlee.laundry.auth.domain.model.RefreshToken
import me.hyunlee.laundry.common.domain.phone.PhoneNumberNormalizer
import me.hyunlee.laundry.common.domain.port.UserDirectoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.*

@Service
class OtpService(
    private val otpRepo: OtpChallengeRepository,
    private val codeGen: OtpCodeGeneratorPort,
    private val clock: ClockPort,
//    private val limiter: RateLimiterPort,
    private val sms: SmsSenderPort,
    private val emailSender: EmailSenderPort,
    private val tokenCodec: TokenCodecPort,
) {
    private val otpTtlSec: Long = 180
    private val maxAttempts: Int = 5

    fun start(userDir: UserDirectoryPort, phone: String): StartLoginResult {
        val user = userDir.findByPhone(phone)
        val exists = user != null
        val channels = if (exists) {
            if (userDir.hasEmail(user)) listOf(SMS, EMAIL) else listOf(SMS)
        } else {
            listOf(SMS, EMAIL)
        }
        val masked = user?.let { userDir.maskedEmailOf(it) }
        val firstName = user?.firstName
        return StartLoginResult(exists = exists, firstName = firstName, channels = channels, maskedEmail = masked)
    }

    @Transactional
    fun send(phone: String, channel: OtpChannel, emailIfNeeded: String? = null) {
//        limiter.checkAndConsume("otp:send:$phone")
        val code = codeGen.generate(6)
        val codeHash = tokenCodec.hash(code)
        val now = clock.now()
        val challenge = OtpChallenge(
            id = UUID.randomUUID(),
            phone = phone,
            channel = channel,
            codeHash = codeHash,
            expiresAt = now.plusSeconds(otpTtlSec),
            maxAttempts = maxAttempts,
            attempts = 0,
            status = PENDING,
            createdAt = now,
        )
        otpRepo.save(challenge)
        when (channel) {
            SMS -> sms.sendOtp(phone, code, otpTtlSec)
            EMAIL -> emailSender.sendOtp(emailIfNeeded ?: "", code, otpTtlSec)
        }
    }

    @Transactional
    fun verify(phone: String, code: String) {
//        limiter.checkAndConsume("otp:verify:$phone")
        val ch = otpRepo.findActiveByPhone(phone) ?: throw AuthException.OtpInvalid()
        val now = clock.now()
        if (now.isAfter(ch.expiresAt)) {
            otpRepo.markBlocked(ch.id)
            throw AuthException.OtpExpired()
        }
        val inputHash = tokenCodec.hash(code)
        // Try to atomically verify when code matches and still pending
        val verified = otpRepo.markVerifiedIfMatch(ch.id, inputHash, now)
        if (verified) return
        // Wrong code path: atomically increment attempts if still pending and below max
        val incremented = otpRepo.incrementAttemptsIfPending(ch.id, now)
        if (incremented) {
            // Attempts incremented successfully → still pending and not expired, return invalid code
            throw AuthException.OtpInvalid()
        } else {
            // Increment failed: either expired or attempts already reached max (or race marked verified/blocked)
            if (now.isAfter(ch.expiresAt)) {
                otpRepo.markBlocked(ch.id)
                throw AuthException.OtpExpired()
            }
            // Reaching here typically means attempts >= max or status changed by another request
            otpRepo.markBlocked(ch.id)
            throw AuthException.OtpAttemptsExceeded()
        }
    }
}

@Service
class TokenService(
    private val tokenCodec: TokenCodecPort,
    private val clock: ClockPort,
    private val rtRepo: RefreshTokenRepository,
) {
    private val accessTtlSec: Long = Duration.ofMinutes(30).seconds
    private val refreshTtlSec: Long = Duration.ofDays(30).seconds

    fun issue(userId: UUID, claims: Map<String, Any> = emptyMap()): TokenPair {
        val now = clock.now()

        val at = tokenCodec.newAccessToken(
            userId = userId,
            ttlSeconds = accessTtlSec,
            claims = claims
        )

        val rt = tokenCodec.newRefreshToken()

        rtRepo.save(
            RefreshToken(
                id = UUID.randomUUID(),
                userId = userId,
                tokenHash = tokenCodec.hash(rt),
                issuedAt = now,
                expiresAt = now.plusSeconds(refreshTtlSec),
            )
        )

        return TokenPair(
            accessToken = at,
            accessTokenExpiresIn = accessTtlSec,
            refreshToken = rt,
            refreshTokenExpiresIn = refreshTtlSec,
        )
    }

    fun consumeRefreshToken(refreshToken: String): UUID {
        val now = clock.now()
        val hash = tokenCodec.hash(refreshToken)
        val found = rtRepo.findByTokenHash(hash) ?: throw AuthException.TokenInvalid()

        if (found.revokedAt != null || now.isAfter(found.expiresAt)) {
            throw AuthException.TokenInvalid()
        }

        rtRepo.revoke(found.id, now)

        return found.userId
    }

    fun logout(refreshToken: String?) {
        val now = clock.now()
        if (refreshToken.isNullOrBlank()) return
        val hash = tokenCodec.hash(refreshToken)
        val found = rtRepo.findByTokenHash(hash) ?: return
        rtRepo.revoke(found.id, now)
    }

    fun logoutAll(userId: UUID) {
        rtRepo.revokeAllByUser(userId, clock.now())
    }
}

@Service
class AuthService(
    private val userDir: UserDirectoryPort,
    private val otpSvc: OtpService,
    private val tokenSvc: TokenService,
    private val phoneSvc: PhoneNumberNormalizer,
) : AuthUseCase {

    override fun start(command: StartLoginCommand): StartLoginResult {
        val phone = normalizePhone(command.phone)
        return otpSvc.start(userDir, phone)
    }

    @Transactional
    override fun sendOtp(command: SendOtpCommand) {
        val phone = normalizePhone(command.phone)
        val start = otpSvc.start(userDir, phone)
        val channel = resolveChannelOrThrow(start, command.channel)
        val user = userDir.findByPhone(phone)

        val email = when (channel) {
            EMAIL -> {
                if (user != null) {
                    user.email ?: throw AuthException.UnsupportedChannel("Email not registered for this user")
                } else {
                    command.email
                        ?: throw AuthException.UnsupportedChannel("Email is required for EMAIL OTP when signing up")
                }
            }

            else -> null
        }

        otpSvc.send(phone, channel, email)
    }

    @Transactional
    override fun verifyOtp(command: VerifyOtpCommand): VerifyOtpResult {
        val phone = normalizePhone(command.phone)
        otpSvc.verify(phone, command.code)
        var user = userDir.findByPhone(phone)

        val isNew = if (user == null) {
            val fn = command.firstName?.takeIf { it.isNotBlank() }
                ?: throw AuthException.MissingSignupFields("firstName is required for sign-up")
            val ln = command.lastName?.takeIf { it.isNotBlank() }
                ?: throw AuthException.MissingSignupFields("lastName is required for sign-up")
            user = userDir.createByPhone(phone, fn, ln, command.email)
            true
        } else false

        val claims = generateClaims(user)

        val pair = tokenSvc.issue(user.id, claims)

        return VerifyOtpResult(pair, isNew)
    }

    override fun refresh(command: RefreshSessionCommand): TokenPair {
        val userId = tokenSvc.consumeRefreshToken(command.refreshToken)

        val user = userDir.findById(userId) ?: throw AuthException.TokenInvalid()

        val claims = generateClaims(user)

        return tokenSvc.issue(user.id, claims)
    }

    private fun generateClaims(user: UserDirectoryPort.UserSummary): Map<String, Any> = mapOf(
        "role" to user.role.name,
        "name" to user.firstName
    )

    override fun logout(command: LogoutCommand) {
        if (command.allSessions) {
            // cannot identify userId from opaque access token here; require refresh token for now
            tokenSvc.logout(command.refreshToken)
        } else tokenSvc.logout(command.refreshToken)
    }

    private fun resolveChannelOrThrow(start: StartLoginResult, req: OtpChannel?): OtpChannel {
        if (req == null) return start.channels.first()
        if (!start.channels.contains(req)) throw AuthException.UnsupportedChannel()
        return req
    }

    private fun normalizePhone(phone: String): String {
        return try {
            phoneSvc.normalizeToE164(phone, null)
        } catch (_: IllegalArgumentException) {
            throw AuthException.InvalidPhone()
        }
    }
}
