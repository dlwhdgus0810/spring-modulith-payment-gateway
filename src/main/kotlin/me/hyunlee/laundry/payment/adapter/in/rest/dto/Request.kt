package me.hyunlee.laundry.payment.adapter.`in`.rest.dto

import me.hyunlee.laundry.common.UserId
import me.hyunlee.laundry.payment.domain.model.WalletType
import me.hyunlee.laundry.payment.domain.port.`in`.CreateCardOrWalletCommand
import java.util.*

data class PaymentMethodRegisterRequest(
    val stripePmId: String,
    val brand: String?,                // "visa" etc
    val last4: String?,                // card last 4 digits
    val expMonth: Int?,              // card only
    val expYear: Int?,               // card only
    val wallet: String,
    val fingerprint: String? = null,   // stripe fingerprint
    val nickname: String? = null,
    val setAsDefault: Boolean = false,
    val idempotentKey: String? = null
) {
    fun toCommnad(userId: UUID): CreateCardOrWalletCommand {
        return CreateCardOrWalletCommand(
            userId = UserId(userId),
            stripePmId = stripePmId,
            brand = brand,                // "visa" etc
            last4 = last4,                // card last 4 digits
            expMonth = expMonth,              // card only
            expYear = expYear,               // card only
            wallet = WalletType.valueOf(wallet),
            fingerprint = fingerprint,   // stripe fingerprint
            nickname = nickname,
            setAsDefault = setAsDefault,
            idempotentKey = idempotentKey
        )
    }
}