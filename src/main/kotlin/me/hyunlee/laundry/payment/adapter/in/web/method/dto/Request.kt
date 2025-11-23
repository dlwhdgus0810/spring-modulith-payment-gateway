package me.hyunlee.laundry.payment.adapter.`in`.web.method.dto

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.common.validation.RequestValidator
import me.hyunlee.laundry.payment.application.port.`in`.method.CreateAchCommand
import me.hyunlee.laundry.payment.application.port.`in`.method.CreateCardCommand
import me.hyunlee.laundry.payment.application.port.`in`.method.CreateWalletCommand
import me.hyunlee.laundry.payment.domain.model.method.AchVerificationStatus
import me.hyunlee.laundry.payment.domain.model.method.WalletType
import java.time.Year
import java.util.*

enum class PaymentMethodRegisterType { CARD, WALLET, ACH }

/* =============== Legacy register request (server-confirm paths) =============== */

data class PaymentMethodRegisterRequest(
    // common
    val type: String,                  // CARD | WALLET | ACH
    val stripePmId: String,
    val nickname: String? = null,
    val setAsDefault: Boolean = false,
    val idempotentKey: String? = null,

    // card/wallet specific
    val brand: String? = null,         // "visa" etc
    val last4: String? = null,         // last 4 digits
    val expMonth: Int? = null,         // card only
    val expYear: Int? = null,          // card only
    val wallet: String? = null,        // WALLET only: "APPLE_PAY" | "GOOGLE_PAY" | "LINK"
    val fingerprint: String? = null,   // stripe fingerprint

    // ach specific
    val bankName: String? = null,
    val achLast4: String? = null,
    val mandateId: String? = null,
    val verification: String? = null   // UNKNOWN | PENDING | VERIFIED | FAILED
) {
    fun validateOrThrow() {
        // type (enum)
        RequestValidator.oneOfEnum(type, PaymentMethodRegisterType::class.java, "type")

        // common PM id
        RequestValidator.notBlank(stripePmId, "stripePmId")
        RequestValidator.startsWith(stripePmId, "pm_", "stripePmId")

        when (PaymentMethodRegisterType.valueOf(type)) {
            PaymentMethodRegisterType.CARD -> {
                RequestValidator.intRange(expMonth, "expMonth", 1, 12)
                RequestValidator.yearNotBefore(expYear, "expYear", Year.now().value)
                RequestValidator.length(last4, "last4", 4)
            }
            PaymentMethodRegisterType.WALLET -> {
                val w = requireNotNull(wallet) { "wallet is required for WALLET" }
                RequestValidator.oneOfEnum(w, WalletType::class.java, "wallet")
                RequestValidator.length(last4, "last4", 4)
                // expMonth/expYear는 WALLET에서 필수가 아님
            }
            PaymentMethodRegisterType.ACH -> {
                RequestValidator.notBlank(bankName, "bankName")
                RequestValidator.notBlank(mandateId, "mandateId")
                RequestValidator.length(achLast4, "achLast4", 4)
            }
        }
    }

    fun toCardCommand(userId: UUID): CreateCardCommand {
        return CreateCardCommand(
            userId = UserId(userId),
            stripePmId = stripePmId,
            brand = brand,
            last4 = last4,
            expMonth = expMonth,
            expYear = expYear,
            fingerprint = fingerprint,
            nickname = nickname,
            setAsDefault = setAsDefault,
            idempotentKey = idempotentKey
        )
    }

    fun toWalletCommand(userId: UUID): CreateWalletCommand {
        val w = requireNotNull(wallet) { "wallet is required for WALLET" }
        return CreateWalletCommand(
            userId = UserId(userId),
            stripePmId = stripePmId,
            wallet = WalletType.valueOf(w),
            brand = brand,
            last4 = last4,
            fingerprint = fingerprint,
            nickname = nickname,
            setAsDefault = setAsDefault,
            idempotentKey = idempotentKey
        )
    }

    fun toAchCommand(userId: UUID): CreateAchCommand {
        val bank = requireNotNull(bankName) { "bankName is required for ACH" }
        val mandate = requireNotNull(mandateId) { "mandateId is required for ACH" }
        val ver = verification?.let { AchVerificationStatus.valueOf(it) } ?: AchVerificationStatus.PENDING
        return CreateAchCommand(
            userId = UserId(userId),
            stripePmId = stripePmId,
            bankName = bank,
            last4 = achLast4,
            mandateId = mandate,
            verification = ver,
            nickname = nickname,
            setAsDefault = setAsDefault,
            idempotentKey = idempotentKey
        )
    }

    // 선택: 하나의 진입점으로 합치고 싶다면
    fun toCommand(userId: UUID) = when (PaymentMethodRegisterType.valueOf(type)) {
        PaymentMethodRegisterType.CARD -> toCardCommand(userId)
        PaymentMethodRegisterType.WALLET -> toWalletCommand(userId)
        PaymentMethodRegisterType.ACH -> toAchCommand(userId)
    }
}

/* =============== New flow: start/finalize SetupIntent (client confirm) =============== */

data class StartSetupIntentRequest(
    val idempotentKey: String
)

data class FinalizeSetupIntentRequest(
    val setupIntentId: String,
    val nickname: String? = null,
    val setAsDefault: Boolean = false
) {
    fun validateOrThrow() {
        RequestValidator.startsWith(setupIntentId, "seti_", "setupIntentId")
    }
}