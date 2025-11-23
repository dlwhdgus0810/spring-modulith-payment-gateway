package me.hyunlee.laundry.payment.adapter.`in`.web.method.dto

data class StartSetupIntentResponse(
    val setupIntentId: String,
    val clientSecret: String?,
    val customerId: String? = null
)

data class PaymentMethodResponse(
    val id: String,
    val isDefault: Boolean,
    val summary: PaymentSummaryResponse?,            // 카드/월렛인 경우만
    val wallet: String?,                     // "APPLE_PAY" | "GOOGLE_PAY" | "LINK" | "NONE"
    val bankName: String?,                   // ACH일 때만
)

data class PaymentSummaryResponse(
    val brand: String?,
    val last4: String?,
    val expMonth: Int?,
    val expYear: Int?
)