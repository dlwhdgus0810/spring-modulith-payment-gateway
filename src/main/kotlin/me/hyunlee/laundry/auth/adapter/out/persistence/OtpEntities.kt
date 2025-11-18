package me.hyunlee.laundry.auth.adapter.out.persistence

import jakarta.persistence.*
import me.hyunlee.laundry.auth.domain.model.OtpChannel
import me.hyunlee.laundry.auth.domain.model.OtpStatus
import me.hyunlee.laundry.common.adapter.out.persistence.BaseEntity
import java.time.Instant
import java.util.*

@Entity
@Table(name = "otp_challenges")
class OtpChallengeEntity(
    @Id
    @Column(columnDefinition = "BINARY(16)", nullable = false)
    var id: UUID,

    @Column(length = 32, nullable = false)
    var phone: String,

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    var channel: OtpChannel,

    @Column(name = "code_hash", length = 128, nullable = false)
    var codeHash: String,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

    @Column(name = "max_attempts", nullable = false)
    var maxAttempts: Int,

    @Column(nullable = false)
    var attempts: Int,

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    var status: OtpStatus,
) : BaseEntity()

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    @Id
    @Column(columnDefinition = "BINARY(16)", nullable = false)
    var id: UUID,

    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    var userId: UUID,

    @Column(name = "token_hash", length = 128, nullable = false, unique = true)
    var tokenHash: String,

    @Column(name = "issued_at", nullable = false)
    var issuedAt: Instant,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant,

    @Column(name = "rotated_from_id", columnDefinition = "BINARY(16)")
    var rotatedFromId: UUID? = null,

    @Column(name = "revoked_at")
    var revokedAt: Instant? = null,
) : BaseEntity()
