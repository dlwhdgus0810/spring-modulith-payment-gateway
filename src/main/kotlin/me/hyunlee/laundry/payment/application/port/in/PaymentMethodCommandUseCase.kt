package me.hyunlee.laundry.payment.application.port.`in`

import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.payment.domain.model.PaymentMethod

interface PaymentMethodCommandUseCase {
    // Legacy/direct create (expects pm_* already tokenized/attached appropriately)
    fun create(command : CreatePaymentMethodCommand) : PaymentMethod

    // New card/wallet onboarding: start and finalize (client-side 3DS confirmation)
    fun startSetupIntent(userId: UserId, paymentMethodType: String = "card", idempotentKey: String? = null): StartSetupIntentResult
    fun finalizeSetupIntent(userId: UserId, setupIntentId: String, nickname: String? = null, setAsDefault: Boolean = false): PaymentMethod

    fun setDefault(userId: UserId, paymentMethodId: PaymentMethodId)
    fun unsetDefault(userId: UserId, paymentMethodId: PaymentMethodId)
}

data class StartSetupIntentResult(
    val setupIntentId: String,
    val clientSecret: String?,
    val customerId: String?
)