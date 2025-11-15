package me.hyunlee.laundry.payment.domain.model.method

import me.hyunlee.laundry.common.domain.UserId

sealed interface NewPaymentSpec {
    val userId: UserId
    val stripePmId: String   // pm_*
    val isDefault: Boolean
    val nickname: String?
}

data class NewCardSpec(
    override val userId: UserId,
    override val stripePmId: String,
    override val nickname: String? = null,
    override val isDefault: Boolean = false,
    val summary: PaymentSummary,
    val fingerprint: String? = null,
) : NewPaymentSpec

data class NewWalletSpec(
    override val userId: UserId,
    override val stripePmId: String,
    override val nickname: String? = null,
    override val isDefault: Boolean = false,
    val wallet: WalletType,
    val summary: PaymentSummary?,
    val fingerprint: String? = null,
) : NewPaymentSpec



data class NewAchSpec(
    override val userId: UserId,
    override val stripePmId: String,
    override val nickname: String? = null,
    override val isDefault: Boolean = false,
    val bankName: String,
    val last4: String?,
    val mandateId: String,
    val verification: AchVerificationStatus = AchVerificationStatus.PENDING,
) : NewPaymentSpec