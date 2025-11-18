package me.hyunlee.laundry.auth.application.port.`in`

import me.hyunlee.laundry.auth.domain.model.OtpChannel

// Commands

data class StartLoginCommand(val phone: String)

data class SendOtpCommand(val phone: String, val channel: OtpChannel? = null, val email: String? = null)

data class VerifyOtpCommand(
    val phone: String,
    val code: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
)

data class RefreshSessionCommand(val refreshToken: String)

data class LogoutCommand(val refreshToken: String?, val allSessions: Boolean = false)

// DTOs / Results

data class StartLoginResult(
    val exists: Boolean,
    val firstName: String?,
    val channels: List<OtpChannel>,
    val maskedEmail: String?
)

data class TokenPair(
    val accessToken: String,
    val accessTokenExpiresIn: Long,
    val refreshToken: String,
    val refreshTokenExpiresIn: Long,
)

data class VerifyOtpResult(
    val tokens: TokenPair,
    val isNewUser: Boolean,
)

interface AuthUseCase {
    fun start(command: StartLoginCommand): StartLoginResult
    fun sendOtp(command: SendOtpCommand)
    fun verifyOtp(command: VerifyOtpCommand): VerifyOtpResult
    fun refresh(command: RefreshSessionCommand): TokenPair
    fun logout(command: LogoutCommand)
}
