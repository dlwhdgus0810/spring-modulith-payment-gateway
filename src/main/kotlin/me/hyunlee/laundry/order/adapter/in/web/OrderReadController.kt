package me.hyunlee.laundry.order.adapter.`in`.web

import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
import me.hyunlee.laundry.order.application.port.`in`.OrderQueryUseCase
import me.hyunlee.laundry.order.domain.model.OrderId
import org.springframework.http.ResponseEntity
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

    @GetMapping("/{orderId}")
    fun get(@PathVariable orderId: OrderId): ResponseEntity<ApiResponse<Any>> {
        val body = query.getById(orderId)
        return ApiResponse.success(body)
    }
}