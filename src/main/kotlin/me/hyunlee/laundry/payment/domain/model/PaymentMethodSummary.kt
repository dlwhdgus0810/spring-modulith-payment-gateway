package me.hyunlee.laundry.payment.domain.model

sealed interface PaymentMethodSummary

enum class PaymentMethodSummaryType { CARD_OR_WALLET, ACH }
enum class WalletType { NONE, APPLE_PAY, GOOGLE_PAY, LINK }
enum class AchVerificationStatus { UNKNOWN, PENDING, VERIFIED, FAILED }

data class CardOrWalletSummary(
    val brand: PaymentBrand?,
    val wallet: WalletType = WalletType.NONE,
    val fingerprint: String? = null,
    val nickname: String? = null
) : PaymentMethodSummary

data class AchSummary(
    val bankName: String,
    val last4: String?,
    val mandateId: String,
    val verification: AchVerificationStatus = AchVerificationStatus.PENDING,
    val nickname: String? = null
) : PaymentMethodSummary

data class PaymentBrand(
    val brand: String?,     // "visa", "mastercard", 은행명 등
    val last4: String?,     // 카드/계좌 끝 4자리
    val expMonth: Int? = null, // 카드만
    val expYear: Int? = null   // 카드만
)