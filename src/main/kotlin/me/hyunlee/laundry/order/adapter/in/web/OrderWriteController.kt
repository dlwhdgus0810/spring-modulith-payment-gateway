package me.hyunlee.laundry.order.adapter.`in`.web

import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.order.adapter.`in`.web.dto.CreateOrderRequest
import me.hyunlee.laundry.order.adapter.`in`.web.dto.OrderResponse
import me.hyunlee.laundry.order.adapter.`in`.web.dto.toResponse
import me.hyunlee.laundry.order.application.port.`in`.OrderCommandUseCase
import org.springframework.http.ResponseEntity
import io.swagger.v3.oas.annotations.Operation
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@Validated
@RestController
@RequestMapping("/api/orders")
class OrderWriteController(
    private val command: OrderCommandUseCase
) {

    @PostMapping
    @Operation(summary = "createOrder")
    fun create(@RequestBody req: CreateOrderRequest, auth: JwtAuthenticationToken): ResponseEntity<ApiResponse<OrderResponse>> {
        val currentUserId = UserId(UUID.fromString(auth.token.claims["sub"] as String))
        val order = command.create(req.toCommand(currentUserId)).toResponse()
        return ApiResponse.created(order)
    }
}