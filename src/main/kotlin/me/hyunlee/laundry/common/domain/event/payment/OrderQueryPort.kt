package me.hyunlee.laundry.common.domain.event.payment

import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import java.util.*

/**
 * 결제 모듈 → 주문 모듈: 주문의 결제 메타정보 조회 포트
 */
interface OrderQueryPort {
    fun getOrderPaymentMeta(orderId: UUID): OrderPaymentMeta?
}

data class OrderPaymentMeta(
    val userId: UserId,
    val paymentMethodId: PaymentMethodId,
)