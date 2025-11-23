package me.hyunlee.laundry.order.adapter.`in`.web

import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
import me.hyunlee.laundry.order.adapter.`in`.web.dto.OrderResponse
import me.hyunlee.laundry.order.adapter.`in`.web.dto.toResponse
import me.hyunlee.laundry.order.application.port.`in`.OrderQueryUseCase
import me.hyunlee.laundry.order.domain.model.OrderId
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import io.swagger.v3.oas.annotations.Operation
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/orders")
class OrderReadController(
    private val query : OrderQueryUseCase
) {

    private val log = LoggerFactory.getLogger(OrderReadController::class.java)

    @GetMapping
    @Operation(summary = "getOrders")
    fun list(): ResponseEntity<ApiResponse<List<OrderResponse>>> {
        val orders = query.getAll().map { it.toResponse() }
        return ApiResponse.success(orders)
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "getOrderById")
    fun get(@PathVariable orderId: OrderId): ResponseEntity<ApiResponse<OrderResponse>> {
        val order = query.getById(orderId).toResponse()
        return ApiResponse.success(order)
    }
}