package me.hyunlee.laundry.auth.adapter.`in`.web

import me.hyunlee.laundry.auth.domain.exception.AuthException
import me.hyunlee.laundry.common.adapter.`in`.web.CommonGlobalExceptionSupport
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class AuthExceptionHandler : CommonGlobalExceptionSupport() {

    @ExceptionHandler(AuthException.InvalidPhone::class)
    fun invalidPhone(ex: AuthException.InvalidPhone): ResponseEntity<ErrorResponse> =
        badRequest("INVALID_PHONE", ex.message ?: "Invalid phone")

    @ExceptionHandler(AuthException.UnsupportedChannel::class)
    fun unsupported(ex: AuthException.UnsupportedChannel): ResponseEntity<ErrorResponse> =
        badRequest("UNSUPPORTED_CHANNEL", ex.message ?: "Unsupported channel")

    @ExceptionHandler(AuthException.OtpExpired::class)
    fun otpExpired(ex: AuthException.OtpExpired): ResponseEntity<ErrorResponse> =
        badRequest("OTP_EXPIRED", ex.message ?: "OTP expired")

    @ExceptionHandler(AuthException.OtpInvalid::class)
    fun otpInvalid(ex: AuthException.OtpInvalid): ResponseEntity<ErrorResponse> =
        badRequest("OTP_INVALID", ex.message ?: "Invalid OTP")

    @ExceptionHandler(AuthException.OtpAttemptsExceeded::class)
    fun otpAttempts(ex: AuthException.OtpAttemptsExceeded): ResponseEntity<ErrorResponse> =
        badRequest("OTP_ATTEMPTS_EXCEEDED", ex.message ?: "Too many attempts")

    @ExceptionHandler(AuthException.RateLimited::class)
    fun rateLimited(ex: AuthException.RateLimited): ResponseEntity<ErrorResponse> =
        badRequest("RATE_LIMITED", ex.message ?: "Too many requests")

    @ExceptionHandler(AuthException.TokenInvalid::class)
    fun tokenInvalid(ex: AuthException.TokenInvalid): ResponseEntity<ErrorResponse> =
        badRequest("TOKEN_INVALID", ex.message ?: "Invalid token")

    @ExceptionHandler(AuthException.MissingSignupFields::class)
    fun missingSignup(ex: AuthException.MissingSignupFields): ResponseEntity<ErrorResponse> =
        badRequest("MISSING_SIGNUP_FIELDS", ex.message ?: "Missing required signup fields")
}
