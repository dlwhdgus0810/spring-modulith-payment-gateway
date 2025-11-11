package me.hyunlee.laundry.common.domain.event.payment

import me.hyunlee.laundry.common.domain.UserId

/**
 * Publisher: payment
 * Consumers: user
 * Semantics: Stripe customer(cus_*) ensured for the user.
 * Version: v1
 */
data class ProviderCustomerEnsuredEvent(
    val userId: UserId,
    val customerId: String,
    val count: Int = 0
)