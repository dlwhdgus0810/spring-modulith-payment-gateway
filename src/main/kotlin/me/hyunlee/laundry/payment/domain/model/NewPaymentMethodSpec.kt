package me.hyunlee.laundry.payment.domain.model

import me.hyunlee.laundry.common.UserId

sealed interface NewPaymentMethodSpec {
    val userId: UserId
    val stripePmId: String   // pm_*
    val setAsDefault: Boolean
}

data class NewCardOrWalletSpec(
    override val userId: UserId,
    override val stripePmId: String,
    val brand: PaymentBrand?,
    val wallet: WalletType = WalletType.NONE,
    val fingerprint: String? = null,
    val nickname: String? = null,
    override val setAsDefault: Boolean = false
) : NewPaymentMethodSpec

data class NewAchSpec(
    override val userId: UserId,
    override val stripePmId: String,
    val bankName: String,
    val last4: String?,
    val mandateId: String,
    val verification: AchVerificationStatus = AchVerificationStatus.PENDING,
    val nickname: String? = null,
    override val setAsDefault: Boolean = false
) : NewPaymentMethodSpec