package me.hyunlee.laundry.user.adapter.`in`.web

import io.swagger.v3.oas.annotations.Operation
import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.user.adapter.`in`.web.dto.UserResponse
import me.hyunlee.laundry.user.adapter.`in`.web.dto.toResponse
import me.hyunlee.laundry.user.application.port.`in`.UserReadPort
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@Validated
@RestController
@RequestMapping("/api/users")
class UserReadController(
    private val query: UserReadPort
) {

    @GetMapping
    @Operation(summary = "getAllUsers")
    fun getAll(): ResponseEntity<ApiResponse<List<UserResponse>>> {
        val users = query.getAll().map { it.toResponse() }
        return ApiResponse.success(users)
    }

    @GetMapping("/{id}")
    @Operation(summary = "getUserById")
    fun get(@PathVariable id: UUID): ResponseEntity<ApiResponse<UserResponse>> {
        val user = query.getById(UserId(id)).toResponse()
        return ApiResponse.success(user)
    }

    @GetMapping("/{phone}")
    @Operation(summary = "getUserByPhone")
    fun getByPhone(@PathVariable phone: String): ResponseEntity<ApiResponse<UserResponse>> {
        val user = query.getByPhone(phone).toResponse()
        return ApiResponse.success(user)
    }
}