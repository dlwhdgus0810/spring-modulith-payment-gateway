package me.hyunlee.laundry.payment.adapter.`in`.rest

import me.hyunlee.laundry.common.ApiResponse
import me.hyunlee.laundry.payment.adapter.`in`.rest.dto.PaymentMethodRegisterRequest
import me.hyunlee.laundry.payment.domain.port.`in`.PaymentMethodCommandUseCase
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@Validated
@RestController
@RequestMapping("/api/payment-methods")
class PaymentMethodWriteController(
    private val command: PaymentMethodCommandUseCase
) {

    @PostMapping("/{userId}")
    fun create(@PathVariable userId : UUID, @RequestBody req : PaymentMethodRegisterRequest) : ResponseEntity<ApiResponse<Any>> {
        val pm = command.create(req.toCommnad(userId))
        return ApiResponse.created(pm)
    }
}