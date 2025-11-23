package me.hyunlee.laundry.auth.adapter.`in`.web.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class StartRequest(
    @field:NotBlank
    val phone: String
)

data class SendOtpRequest(
    @field:NotBlank
    val phone: String,
    val channel: String? = null,
    @Email
    val email: String? = null,
)

data class VerifyOtpRequest(
    @field:NotBlank
    val phone: String,
    @field:NotBlank
    val code: String,
    val firstName: String? = null,
    val lastName: String? = null,
    @Email
    val email: String? = null,
)

data class RefreshRequest(
    @field:NotBlank
    val refreshToken: String
)

data class LogoutRequest(
    val refreshToken: String? = null,
    val allSessions: Boolean? = false
)
