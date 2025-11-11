package me.hyunlee.laundry.payment.domain.model

sealed interface PaymentInfo

enum class PaymentMethodType { CARD, WALLET, ACH }
enum class WalletType { APPLE_PAY, GOOGLE_PAY, LINK }
enum class AchVerificationStatus { UNKNOWN, PENDING, VERIFIED, FAILED }

data class CardInfo(
    val summary: PaymentSummary,        // 카드인 경우 expMonth/expYear 필수 권장
    val fingerprint: String? = null,
) : PaymentInfo

data class WalletInfo(
    val wallet: WalletType,             // APPLE_PAY | GOOGLE_PAY | LINK
    val summary: PaymentSummary?,       // 제공되는 경우만(브랜드/last4)
    val fingerprint: String? = null,
) : PaymentInfo

data class AchInfo(
    val bankName: String,
    val last4: String?,
    val mandateId: String,
    val verification: AchVerificationStatus = AchVerificationStatus.PENDING,
) : PaymentInfo

data class PaymentSummary(
    val brand: String?,    // "visa", "mastercard" 등
    val last4: String?,
    val expMonth: Int? = null,
    val expYear: Int? = null
)