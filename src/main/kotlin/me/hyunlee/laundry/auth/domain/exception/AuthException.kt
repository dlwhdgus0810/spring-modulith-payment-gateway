package me.hyunlee.laundry.auth.domain.exception

sealed class AuthException(message: String) : RuntimeException(message) {
    class InvalidPhone(message: String = "Invalid phone number") : AuthException(message)
    class UnsupportedChannel(message: String = "Unsupported auth channel") : AuthException(message)
    class OtpExpired(message: String = "OTP expired") : AuthException(message)
    class OtpInvalid(message: String = "Invalid OTP code") : AuthException(message)
    class OtpAttemptsExceeded(message: String = "Too many attempts") : AuthException(message)
    class RateLimited(message: String = "Too many requests") : AuthException(message)
    class TokenInvalid(message: String = "Invalid token") : AuthException(message)
    class MissingSignupFields(message: String = "Missing required signup fields") : AuthException(message)
}
