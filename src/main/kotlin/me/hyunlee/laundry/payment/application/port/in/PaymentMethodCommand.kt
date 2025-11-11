package me.hyunlee.laundry.payment.application.port.`in`

import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.payment.domain.model.*

/**
 * Command aggregate for PaymentMethod domain.
 * - Create* : register new payment method
 * - Set* Unset* : manage default payment method
 * - Activate / Deactivate / Detach : state transitions
 * - Rename : change nickname / display name
 * - Refresh*Snapshot : sync from Stripe latest data
 */
sealed interface PaymentMethodCommand

interface CreatePaymentMethodCommand : PaymentMethodCommand {
    val userId: UserId
    val stripePmId: String
    val nickname: String?
    val setAsDefault: Boolean
    val idempotentKey: String?
    fun toSpec(): NewPaymentSpec
}

// 카드 전용
data class CreateCardCommand(
    override val userId: UserId,
    override val stripePmId: String,        // pm_*
    val brand: String?,
    val last4: String?,
    val expMonth: Int?,            // 카드만 필수 (검증은 service/domain에서)
    val expYear: Int?,
    val fingerprint: String? = null,
    override val nickname: String? = null,
    override val setAsDefault: Boolean = false,
    override val idempotentKey: String? = null,
) : CreatePaymentMethodCommand {
    override fun toSpec(): NewCardSpec = NewCardSpec(
        userId = userId,
        stripePmId = stripePmId,
        summary = PaymentSummary(
            brand = brand,
            last4 = last4,
            expMonth = expMonth,
            expYear = expYear,
        ),
        fingerprint = fingerprint,
        nickname = nickname,
        isDefault = setAsDefault,
    )
}

// 월렛 전용
data class CreateWalletCommand(
    override val userId: UserId,
    override val stripePmId: String,        // pm_*
    val wallet: WalletType,        // APPLE_PAY | GOOGLE_PAY | LINK
    val brand: String? = null,
    val last4: String? = null,
    val fingerprint: String? = null,
    override val nickname: String? = null,
    override val setAsDefault: Boolean = false,
    override val idempotentKey: String? = null,
) : CreatePaymentMethodCommand {
    override fun toSpec(): NewWalletSpec = NewWalletSpec(
        userId = userId,
        stripePmId = stripePmId,
        wallet = wallet,
        summary = PaymentSummary(
            brand = brand,
            last4 = last4,
            expMonth = null,
            expYear = null,
        ), // 월렛은 summary가 없을 수도 있어 nullable 허용. 없애고 싶으면 null로.
        fingerprint = fingerprint,
        nickname = nickname,
        isDefault = setAsDefault,
    )
}

// ACH (현행 유지)
data class CreateAchCommand(
    override val userId: UserId,
    override val stripePmId: String,        // pm_*
    val bankName: String,
    val last4: String?,
    val mandateId: String,
    val verification: AchVerificationStatus = AchVerificationStatus.PENDING,
    override val nickname: String? = null,
    override val setAsDefault: Boolean = false,
    override val idempotentKey: String? = null,
) : CreatePaymentMethodCommand {
    override fun toSpec(): NewAchSpec = NewAchSpec(
        userId = userId,
        stripePmId = stripePmId,
        bankName = bankName,
        last4 = last4,
        mandateId = mandateId,
        verification = verification,
        nickname = nickname,
        isDefault = setAsDefault,
    )
}
/* =============== DEFAULT METHOD =============== */

data class SetDefaultCommand(
    val userId: UserId,
    val paymentMethodId: PaymentMethodId
) : PaymentMethodCommand

data class UnsetDefaultCommand(
    val userId: UserId,
    val paymentMethodId: PaymentMethodId
) : PaymentMethodCommand

/* =============== STATE TRANSITIONS =============== */

data class ActivatePaymentMethodCommand(
    val paymentMethodId: PaymentMethodId
) : PaymentMethodCommand

data class DeactivatePaymentMethodCommand(
    val paymentMethodId: PaymentMethodId
) : PaymentMethodCommand

/** PaymentMethod is detached from Stripe customer (webhook) */
data class DetachPaymentMethodCommand(
    val paymentMethodId: PaymentMethodId
) : PaymentMethodCommand

/* =============== METADATA CHANGES =============== */

data class RenamePaymentMethodCommand(
    val paymentMethodId: PaymentMethodId,
    val nickname: String?
) : PaymentMethodCommand

/* =============== SNAPSHOT REFRESH =============== */

/** Refresh card/wallet snapshot from Stripe */
data class RefreshCardSnapshotCommand(
    val paymentMethodId: PaymentMethodId,
    val brand: String?,
    val last4: String?,
    val expMonth: Int?,
    val expYear: Int?,
    val fingerprint: String?,
) : PaymentMethodCommand

/** Refresh ACH snapshot (verification / bank name / mandate) */
data class RefreshAchSnapshotCommand(
    val paymentMethodId: PaymentMethodId,
    val bankName: String?,
    val last4: String?,
    val mandateId: String?,
    val verification: AchVerificationStatus
) : PaymentMethodCommand