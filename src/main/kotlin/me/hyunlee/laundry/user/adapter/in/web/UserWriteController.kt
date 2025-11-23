package me.hyunlee.laundry.user.adapter.`in`.web

import io.swagger.v3.oas.annotations.Operation
import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
import me.hyunlee.laundry.user.adapter.`in`.web.dto.UserResponse
import me.hyunlee.laundry.user.adapter.`in`.web.dto.toResponse
import me.hyunlee.laundry.user.application.port.`in`.UserWritePort
import me.hyunlee.user.adapter.`in`.web.dto.AddAddressRequest
import me.hyunlee.user.adapter.`in`.web.dto.RegisterUserRequest
import me.hyunlee.user.adapter.`in`.web.dto.UpdateUserProfileRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@Validated
@RestController
@RequestMapping("/api/users")
class UserWriteController(
    private val command : UserWritePort
) {

    @PostMapping
    @Operation(summary = "registerUser")
    suspend fun register(@RequestBody req: RegisterUserRequest): ResponseEntity<ApiResponse<UserResponse>> {
        val saved = command.register(req.toCommand()).toResponse()
        return ApiResponse.success(saved)
    }

    @PutMapping
    @Operation(summary = "updateUserProfile")
    suspend fun updateProfile(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody req: UpdateUserProfileRequest) : ResponseEntity<ApiResponse<UserResponse>> {
        val userId = UUID.fromString(jwt.subject)
        val updated = command.updateProfile(req.toCommand(userId)).toResponse()
        return ApiResponse.success(updated)
    }

    @PostMapping("/addresses")
    @Operation(summary = "addUserAddress")
    suspend fun addAddress(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody req: AddAddressRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val userId = UUID.fromString(jwt.subject)
        val saved = command.addAddress(req.toCommand(userId)).toResponse()
        return ApiResponse.success(saved)
    }
}