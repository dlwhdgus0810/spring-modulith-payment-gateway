package me.hyunlee.laundry.auth.adapter.`in`.web.dto

/**
 * verify-otp 성공 시 반환 바디: Access 토큰만 바디로 내려주고, Refresh 토큰은 HttpOnly 쿠키로 설정합니다.
 */
data class AccessTokenResponse(
    val accessToken: String,
    val accessTokenExpiresIn: Long,
    val isNewUser: Boolean
)

/**
 * refresh 시 반환 바디: 새 Access 토큰만 바디로 내려주고, Refresh 토큰은 HttpOnly 쿠키로 회전하여 재설정합니다.
 */
data class AccessTokenOnly(
    val accessToken: String,
    val accessTokenExpiresIn: Long
)
