package me.hyunlee.laundry.payment.application.port.`in`

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.payment.domain.model.PaymentMethod

interface PaymentMethodQueryUseCase {
    fun listByUser(userId: UserId): List<PaymentMethod>
    fun getDefault(userId: UserId): PaymentMethod?
}