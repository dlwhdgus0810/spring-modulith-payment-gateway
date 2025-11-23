package me.hyunlee.laundry.payment.adapter.`in`.web.transaction

import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
import me.hyunlee.laundry.payment.application.port.`in`.transaction.PaymentTransactionProviderPort
import me.hyunlee.laundry.payment.application.port.out.transaction.PaymentTransactionRepository
import org.springframework.http.ResponseEntity
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/payments")
class PaymentTransactionReadController(
    private val txRepo: PaymentTransactionRepository,
    private val txProvider: PaymentTransactionProviderPort,
) {

    /**
     * 주문의 PaymentIntent 상태를 조회합니다.
     * - 아직 선승인이 생성되지 않았다면 status=PENDING을 반환합니다.
     * - clientSecret은 생성 시점에만 제공되므로 조회 응답에는 포함하지 않습니다(null).
     */
    @GetMapping("/orders/{orderId}/payment-intent")
    @Operation(summary = "getPaymentIntentByOrderId")
    fun getPaymentIntent(@PathVariable orderId: UUID): ResponseEntity<ApiResponse<Map<String, String?>>> {
        val tx = txRepo.findByOrderId(orderId)
        if (tx == null) {
            val pending = mapOf(
                "paymentIntentId" to null,
                "clientSecret" to null,
                "status" to "PENDING"
            )
            return ApiResponse.success(pending)
        }

        val snapshot = txProvider.retrievePaymentIntent(tx.paymentIntentId)
        val body = mapOf(
            "paymentIntentId" to snapshot.id,
            "clientSecret" to null,
            "status" to (snapshot.status ?: tx.status.name)
        )
        return ApiResponse.success(body)
    }

    /**
     * 프런트엔드 통합용: 주문의 결제 상태 요약을 조회합니다.
     * - 경로: GET /api/payments/{orderId}
     * - 응답: PaymentStatusRes 스키마
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "getPaymentByOrderId")
    fun getPaymentStatus(@PathVariable orderId: UUID): ResponseEntity<ApiResponse<PaymentStatusRes>> {
        val tx = txRepo.findByOrderId(orderId)
        if (tx == null) {
            val pending = PaymentStatusRes(
                paymentIntentId = null,
                status = "PENDING",
                amountAuthorized = null,
                amountCapturable = null,
                amountCaptured = null,
                amountRefunded = null,
                currency = null,
                receiptUrl = null,
            )
            return ApiResponse.success(pending)
        }

        val snapshot = txProvider.retrievePaymentIntent(tx.paymentIntentId)
        val body = PaymentStatusRes(
            paymentIntentId = snapshot.id,
            status = snapshot.status ?: tx.status.name,
            amountAuthorized = tx.amountAuthorized,
            amountCapturable = snapshot.amountCapturable,
            amountCaptured = tx.amountCaptured,
            amountRefunded = tx.amountRefunded,
            currency = snapshot.currency ?: tx.currency,
            receiptUrl = snapshot.latestChargeReceiptUrl,
        )
        return ApiResponse.success(body)
    }
}

data class PaymentStatusRes(
    val paymentIntentId: String?,
    val status: String?, // requires_capture | succeeded | requires_confirmation | ...
    val amountAuthorized: Long? = null,
    val amountCapturable: Long? = null,
    val amountCaptured: Long? = null,
    val amountRefunded: Long? = null,
    val currency: String? = null,
    val receiptUrl: String? = null,
)
