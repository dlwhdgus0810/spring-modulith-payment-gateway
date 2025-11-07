package me.hyunlee.laundry.payment.domain.port.`in`

import me.hyunlee.laundry.payment.domain.model.PaymentMethod

interface PaymentMethodCommandUseCase {
    fun create(command : CreateCardOrWalletCommand) : PaymentMethod
}