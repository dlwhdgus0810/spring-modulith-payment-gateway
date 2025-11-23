package me.hyunlee.laundry.payment.adapter.`in`.web.method

import io.swagger.v3.oas.annotations.Operation
import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.payment.adapter.`in`.web.method.dto.PaymentMethodResponse
import me.hyunlee.laundry.payment.application.port.`in`.method.PaymentMethodQueryUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/payment-methods")
class PaymentMethodReadController(
    private val query: PaymentMethodQueryUseCase
) {

    @GetMapping
    @Operation(summary = "getPaymentMethods")
    fun list(auth: JwtAuthenticationToken): ResponseEntity<ApiResponse<Map<String, List<PaymentMethodResponse>>>> {
        val userId = UUID.fromString(auth.token.claims["sub"] as String)
        val pms = query.listByUser(UserId(userId)).map { it.toResponse() }
        return ApiResponse.success(mapOf("items" to pms))
    }

    @GetMapping("/default")
    @Operation(summary = "getDefaultPaymentMethod")
    fun getDefault(auth: JwtAuthenticationToken): ResponseEntity<ApiResponse<Map<String, PaymentMethodResponse?>>> {
        val userId = UUID.fromString(auth.token.claims["sub"] as String)
        val pm = query.getDefault(UserId(userId))?.toResponse()
        return ApiResponse.success(mapOf("default" to pm))
    }
}
