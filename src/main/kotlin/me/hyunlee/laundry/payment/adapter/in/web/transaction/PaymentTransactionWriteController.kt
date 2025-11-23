package me.hyunlee.laundry.payment.adapter.`in`.web.transaction

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
import me.hyunlee.laundry.common.domain.event.payment.OrderQueryPort
import me.hyunlee.laundry.payment.application.port.`in`.transaction.PaymentTransactionUseCase
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@Validated
@RestController
@RequestMapping("/api/payments")
class PaymentTransactionWriteController(
    private val orderQuery: OrderQueryPort,
    private val command: PaymentTransactionUseCase,
) {
    private val log = LoggerFactory.getLogger(PaymentTransactionWriteController::class.java)

    // 주문 시점: 선승인 시작 (클라이언트 on-session confirm 전제)
    @PostMapping("/{orderId}/authorize")
    @Operation(summary = "authorizePayment")
    fun authorize(@PathVariable orderId: UUID): ResponseEntity<ApiResponse<Any>> {
        val meta = orderQuery.getOrderPaymentMeta(orderId) ?: error("Order not found: $orderId")
        val result = command.authorizeWithPaymentMethod(
            orderId = orderId,
            userId = meta.userId,
            pmId = meta.paymentMethodId,
            expectedAmount = 10_000,
        )
        return ApiResponse.created(result)
    }

    // 완료 시점: 캡처(부분 캡처 허용)
    @PostMapping("/{orderId}/capture")
    @Operation(summary = "capturePayment")
    fun capture(
        @PathVariable orderId: UUID,
        @RequestBody req: CaptureRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val result = command.capture(orderId, req.amount)
        return ApiResponse.success(result)
    }

    // 클라이언트 on-session confirm 완료 후 상태 동기화 (REQUIRES_CONFIRMATION -> REQUIRES_CAPTURE)
    @PostMapping("/{orderId}/authorize/finalize")
    @Operation(summary = "finalizeAuthorization")
    fun finalizeAuthorize(
        @PathVariable orderId: UUID,
        @RequestBody req: FinalizeAuthorizeRequest
    ): ResponseEntity<ApiResponse<Any>> {
        require(req.paymentIntentId.isNotBlank()) { "paymentIntentId is required" }
        val result = command.finalizeAuthorize(orderId, req.paymentIntentId)
        return ApiResponse.success(result)
    }

    // 선승인 취소(캡처 전)
    @PostMapping("/{orderId}/cancel-authorization")
    @Operation(summary = "cancelAuthorization")
    fun cancelAuthorization(
        @PathVariable orderId: UUID
    ): ResponseEntity<ApiResponse<Any>> {
        val ok = command.cancelAuthorization(orderId)
        return ApiResponse.success(mapOf("canceled" to ok))
    }

    // 환불 생성(전액 또는 부분). 운영/오너 전용 엔드포인트 가정.
    @PostMapping("/{orderId}/refunds")
    @Operation(summary = "refundPayment")
    fun refund(
        @PathVariable orderId: UUID,
        @RequestBody req: RefundRequest
    ): ResponseEntity<ApiResponse<Any>> {
        req.validateOrThrow()
        val result = command.refund(
            orderId = orderId,
            amount = req.amount,
            reason = req.reason,
            idempotentKey = req.idempotentKey
        )
        return ApiResponse.created(result)
    }
}

data class CaptureRequest(
    @field:Min(1) val amount: Long?
)

data class FinalizeAuthorizeRequest(
    @field:NotBlank val paymentIntentId: String
)

data class RefundRequest(
    @field:Min(1) val amount: Long?,
    val reason: String? = null,
    @field:NotBlank val idempotentKey: String,
)

private fun RefundRequest.validateOrThrow() {
    // amount가 null이면 전액 환불로 간주, 지정된 경우는 1 이상
    if (amount != null) require(amount > 0) { "amount must be positive if provided" }
    require(idempotentKey.isNotBlank()) { "idempotentKey required" }
}
