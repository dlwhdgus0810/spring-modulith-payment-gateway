package me.hyunlee.laundry.payment.application.port.`in`.method

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.payment.domain.model.method.PaymentMethod

interface PaymentMethodQueryUseCase {
    fun listByUser(userId: UserId): List<PaymentMethod>
    fun getDefault(userId: UserId): PaymentMethod?
}