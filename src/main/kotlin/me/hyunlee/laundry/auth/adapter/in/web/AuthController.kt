package me.hyunlee.laundry.auth.adapter.`in`.web

import me.hyunlee.laundry.auth.adapter.`in`.web.dto.*
import me.hyunlee.laundry.auth.application.port.`in`.*
import me.hyunlee.laundry.auth.domain.model.OtpChannel
import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
import org.springframework.http.HttpHeaders
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val useCase: AuthUseCase
) {

    @PostMapping("/start")
    @Operation(summary = "startAuth")
    fun start(@RequestBody req: StartRequest): ResponseEntity<ApiResponse<StartLoginResult>> {
        val res = useCase.start(StartLoginCommand(phone = req.phone))
        return ApiResponse.success(res)
    }

    @PostMapping("/send-otp")
    @Operation(summary = "sendOtp")
    fun sendOtp(@RequestBody req: SendOtpRequest): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val channel = req.channel?.let { OtpChannel.valueOf(it) }
        useCase.sendOtp(SendOtpCommand(phone = req.phone, channel = channel, email = req.email))
        return ApiResponse.success(mapOf("sent" to true))
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "verifyOtp")
    fun verifyOtp(@RequestBody req: VerifyOtpRequest): ResponseEntity<ApiResponse<AccessTokenResponse>> {
        val res = useCase.verifyOtp(
            VerifyOtpCommand(
                phone = req.phone,
                code = req.code,
                firstName = req.firstName,
                lastName = req.lastName,
                email = req.email,
            )
        )

        val refresh = res.tokens.refreshToken
        val refreshMaxAge = res.tokens.refreshTokenExpiresIn
        val cookie = buildRefreshCookie(refresh, refreshMaxAge)
        val body = AccessTokenResponse(
            accessToken = res.tokens.accessToken,
            accessTokenExpiresIn = res.tokens.accessTokenExpiresIn,
            isNewUser = res.isNewUser
        )

        val api: ApiResponse<AccessTokenResponse> = ApiResponse(success = true, message = "Success", data = body)
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(api)
    }

    @PostMapping("/refresh")
    @Operation(summary = "refreshToken")
    fun refresh(@CookieValue(name = "refresh_token", required = false) refreshCookie: String?): ResponseEntity<ApiResponse<AccessTokenOnly>> {
        require(!refreshCookie.isNullOrBlank()) { "Missing refresh token cookie" }

        val tokens = useCase.refresh(RefreshSessionCommand(refreshToken = refreshCookie))
        val cookie = buildRefreshCookie(tokens.refreshToken, tokens.refreshTokenExpiresIn)

        val body = AccessTokenOnly(
            accessToken = tokens.accessToken,
            accessTokenExpiresIn = tokens.accessTokenExpiresIn
        )

        val api: ApiResponse<AccessTokenOnly> = ApiResponse(success = true, message = "Success", data = body)
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(api)
    }

    @PostMapping("/logout")
    @Operation(summary = "logout")
    fun logout(
        @RequestBody req: LogoutRequest,
        @CookieValue(name = "refresh_token", required = false) refreshCookie: String?
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        useCase.logout(LogoutCommand(refreshToken = refreshCookie, allSessions = req.allSessions ?: false))
        val expired = ResponseCookie.from("refresh_token", "")
            .httpOnly(true)
            .secure(true)
            .path("/api/auth/refresh")
            .sameSite("Lax")
            .maxAge(0)
            .build()
        val api: ApiResponse<Map<String, Any>> = ApiResponse(success = true, message = "Success", data = mapOf<String, Any>("ok" to true))
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, expired.toString())
            .body(api)
    }

    private fun buildRefreshCookie(value: String, maxAgeSeconds: Long): ResponseCookie =
        ResponseCookie.from("refresh_token", value)
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/api/auth/refresh")
            .maxAge(maxAgeSeconds)
            .build()
}
