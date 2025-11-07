package me.hyunlee.laundry.payment.domain.model

import me.hyunlee.laundry.common.PaymentMethodId
import me.hyunlee.laundry.common.UserId
import java.time.Clock
import java.time.Instant
import java.time.Year

enum class PaymentMethodStatus { ACTIVE, INACTIVE, DETACHED }

data class PaymentMethod(
    val id: PaymentMethodId = PaymentMethodId.newId(),
    val userId: UserId,
    val providerPaymentMethodId: String, // e.g., Stripe pm_*
    val summary: PaymentMethodSummary,
    val status: PaymentMethodStatus,
    val isDefault: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun create(spec: NewPaymentMethodSpec, clock: Clock = Clock.systemUTC()): PaymentMethod {
            
            require(spec.stripePmId.startsWith("pm_")) { "Invalid Stripe PM id" }

            val now = Instant.now(clock)
            val yearNow = Year.now(clock).value

            val summary: PaymentMethodSummary = when (spec) {
                is NewCardOrWalletSpec -> {

                    if (spec.wallet == WalletType.NONE) {
                        require(spec.brand?.expMonth != null && spec.brand.expMonth in 1..12) { "Invalid expMonth" }
                        require(spec.brand.expYear != null && spec.brand.expYear >= yearNow) { "Invalid expYear" }
                    }

                    spec.brand?.last4?.let { require(it.length == 4) { "last4 must be 4 chars" } }

                    CardOrWalletSummary(
                        brand = spec.brand,                  // PaymentBrand?
                        wallet = spec.wallet,
                        fingerprint = spec.fingerprint,
                        nickname = spec.nickname
                    )
                }

                is NewAchSpec -> {
                    require(spec.bankName.isNotBlank()) { "bankName required" }
                    require(spec.mandateId.isNotBlank()) { "mandateId required" }

                    spec.last4?.let { require(it.length == 4) { "last4 must be 4 chars" } }

                    AchSummary(
                        bankName = spec.bankName,
                        last4 = spec.last4,
                        mandateId = spec.mandateId,
                        verification = spec.verification,
                        nickname = spec.nickname
                    )
                }
            }

            return PaymentMethod(
                id = PaymentMethodId.newId(),
                userId = spec.userId,
                providerPaymentMethodId = spec.stripePmId,
                summary = summary,
                status = PaymentMethodStatus.ACTIVE,
                isDefault = spec.setAsDefault,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun touch(clock: Clock = Clock.systemUTC()): PaymentMethod =
        copy(updatedAt = Instant.now(clock))
}