package me.hyunlee.laundry.payment.domain.port.`in`

import me.hyunlee.laundry.common.PaymentMethodId
import me.hyunlee.laundry.common.UserId
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

/* =============== CREATE =============== */

/** Card / Wallet (Apple Pay / Google Pay / Link) registration */
data class CreateCardOrWalletCommand(
    val userId: UserId,
    val stripePmId: String,            // pm_*
    val brand: String?,                // "visa" etc
    val last4: String?,                // card last 4 digits
    val expMonth: Int?,              // card only
    val expYear: Int?,               // card only
    val wallet: WalletType = WalletType.NONE,
    val fingerprint: String? = null,   // stripe fingerprint
    val nickname: String? = null,
    val setAsDefault: Boolean = false,
    val idempotentKey: String? = null
) : PaymentMethodCommand {
    fun toSpec() : NewCardOrWalletSpec {
        val brandVO = PaymentBrand(
            brand = this.brand,
            last4 = this.last4,
            expMonth = this.expMonth,
            expYear = this.expYear
        )

        return NewCardOrWalletSpec(
            userId = userId,
            stripePmId = stripePmId,
            brand = brandVO,
            wallet = wallet,
            fingerprint = fingerprint,
            nickname = nickname,
            setAsDefault = setAsDefault
        )
    }
}

/** ACH registration (bank account) */
data class CreateAchCommand(
    val userId: UserId,
    val stripePmId: String,            // pm_*
    val bankName: String,              // display
    val last4: String?,                // last 4 digits of bank account
    val mandateId: String,             // stripe mandate id
    val verification: AchVerificationStatus = AchVerificationStatus.PENDING,
    val nickname: String? = null,
    val setAsDefault: Boolean = false,
    val idempotentKey: String? = null
) : PaymentMethodCommand {
    fun toSpec(): NewAchSpec = NewAchSpec(
        userId = userId,
        stripePmId = stripePmId,
        bankName = bankName,
        last4 = last4,
        mandateId = mandateId,
        verification = verification,
        nickname = nickname,
        setAsDefault = setAsDefault
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
    val wallet: WalletType = WalletType.NONE
) : PaymentMethodCommand

/** Refresh ACH snapshot (verification / bank name / mandate) */
data class RefreshAchSnapshotCommand(
    val paymentMethodId: PaymentMethodId,
    val bankName: String?,
    val last4: String?,
    val mandateId: String?,
    val verification: AchVerificationStatus
) : PaymentMethodCommand