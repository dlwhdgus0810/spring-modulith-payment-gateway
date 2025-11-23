package me.hyunlee.laundry.payment.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "payments")
data class PaymentsProperties(
    val stripe: Stripe = Stripe()
) {
    data class Stripe(
        val privateKey: String = "",
        val publicKey: String? = null,
        val webhookSigningSecret: String = "",
        val customer: Customer = Customer()
    )

    data class Customer(
        val searchByMetadata: Boolean = true
    )
}
