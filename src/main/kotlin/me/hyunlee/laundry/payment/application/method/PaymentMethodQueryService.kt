package me.hyunlee.laundry.payment.application.method

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.payment.application.port.`in`.method.PaymentMethodQueryUseCase
import me.hyunlee.laundry.payment.application.port.out.method.PaymentMethodRepository
import me.hyunlee.laundry.payment.domain.model.method.PaymentMethod
import org.springframework.stereotype.Service

@Service
class PaymentMethodQueryService(
    private val repo: PaymentMethodRepository
) : PaymentMethodQueryUseCase {
    override fun listByUser(userId: UserId): List<PaymentMethod> = repo.findByUser(userId)
    override fun getDefault(userId: UserId): PaymentMethod? = repo.findDefaultByUser(userId)
}