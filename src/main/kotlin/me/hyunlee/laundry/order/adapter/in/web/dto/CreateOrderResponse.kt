package me.hyunlee.laundry.order.adapter.`in`.web.dto

/**
 * 주문 생성 응답 DTO
 * - 결제는 이벤트 기반으로 비동기 선승인을 수행하므로, 초기 payment.status는 PENDING으로 내려줍니다.
 */
data class CreateOrderResponse(
    val orderId: String,
    val status: String,
    val payment: PaymentSummary
) {
    data class PaymentSummary(
        val status: String,
        val paymentIntentId: String? = null,
        val clientSecret: String? = null
    )
}