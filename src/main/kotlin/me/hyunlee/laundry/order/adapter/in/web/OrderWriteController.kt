package me.hyunlee.laundry.order.adapter.`in`.web

import me.hyunlee.laundry.common.adapter.ApiResponse
import me.hyunlee.laundry.order.adapter.`in`.web.dto.CreateOrderRequest
import me.hyunlee.laundry.order.application.port.`in`.OrderCommandUseCase
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/orders")
class OrderWriteController(
    private val command: OrderCommandUseCase
) {

    @PostMapping
    fun create(@RequestBody req: CreateOrderRequest): ResponseEntity<ApiResponse<Any>> {
        val order = command.create(req.toCommand())
//        val location = URI.create("/api/orders/${order.id}")
        return ApiResponse.created(order)
    }
}