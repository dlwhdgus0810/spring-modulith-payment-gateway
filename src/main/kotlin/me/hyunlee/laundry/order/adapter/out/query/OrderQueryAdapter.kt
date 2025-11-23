package me.hyunlee.laundry.order.adapter.out.query

import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.event.payment.OrderPaymentMeta
import me.hyunlee.laundry.common.domain.event.payment.OrderQueryPort
import me.hyunlee.laundry.order.application.port.out.OrderRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class OrderQueryAdapter(
    private val repo: OrderRepository
) : OrderQueryPort {

    override fun getOrderPaymentMeta(orderId: UUID): OrderPaymentMeta? {
        val order = repo.findById(orderId) ?: return null
        return OrderPaymentMeta(
            userId = order.userId,
            paymentMethodId = PaymentMethodId(order.payment.methodId.value)
        )
    }
}
