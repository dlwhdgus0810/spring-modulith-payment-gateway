package me.hyunlee.laundry.payment.domain.model

import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.payment.application.port.`in`.RetrievedPmInfo
import java.time.Clock
import java.time.Instant
import java.time.Year

enum class PaymentMethodStatus { ACTIVE, INACTIVE, DETACHED }

data class PaymentMethod(
    val id: PaymentMethodId = PaymentMethodId.newId(),
    val userId: UserId,
    val providerPmId: String, // e.g., Stripe pm_*
    val info: PaymentInfo,
    val status: PaymentMethodStatus,
    val isDefault: Boolean = false,
    val nickname: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun create(spec: NewPaymentSpec, clock: Clock = Clock.systemUTC()): PaymentMethod {
            require(spec.stripePmId.startsWith("pm_")) { "Invalid Stripe PM id" }

            val now = Instant.now(clock)
            val yearNow = Year.now(clock).value

            val paymentInfo: PaymentInfo = when (spec) {

                is NewCardSpec -> {
                    require(spec.summary.expMonth != null && spec.summary.expMonth in 1..12) { "Invalid expMonth" }
                    require(spec.summary.expYear != null && spec.summary.expYear >= yearNow) { "Invalid expYear" }

                    spec.summary.last4?.let { require(it.length == 4) { "last4 must be 4 chars" } }

                    CardInfo(summary = spec.summary, fingerprint = spec.fingerprint)
                }

                is NewWalletSpec -> {
                    WalletInfo(
                        wallet = spec.wallet,
                        summary = spec.summary,
                        fingerprint = spec.fingerprint
                    )
                }

                is NewAchSpec -> {
                    require(spec.bankName.isNotBlank()) { "bankName required" }
                    require(spec.mandateId.isNotBlank()) { "mandateId required" }

                    spec.last4?.let { require(it.length == 4) { "last4 must be 4 chars" } }

                    AchInfo(
                        bankName = spec.bankName,
                        last4 = spec.last4,
                        mandateId = spec.mandateId,
                        verification = spec.verification,
                    )
                }
            }

            return PaymentMethod(
                id = PaymentMethodId.newId(),
                userId = spec.userId,
                providerPmId = spec.stripePmId,
                info = paymentInfo,
                status = PaymentMethodStatus.ACTIVE,
                isDefault = spec.isDefault,
                nickname = spec.nickname,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    fun touch(clock: Clock = Clock.systemUTC()): PaymentMethod =
        copy(updatedAt = Instant.now(clock))

    fun markDetached(clock: Clock = Clock.systemUTC()): PaymentMethod =
        copy(status = PaymentMethodStatus.DETACHED).touch(clock)

    fun refreshFrom(info: RetrievedPmInfo, clock: Clock = Clock.systemUTC()): PaymentMethod {
        val newInfo: PaymentInfo = when (info) {
            is RetrievedPmInfo.Card -> {
                val current = this.info as? CardInfo
                CardInfo(summary = info.summary, fingerprint = current?.fingerprint)
            }
            is RetrievedPmInfo.Wallet -> {
                val current = this.info as? WalletInfo
                WalletInfo(wallet = info.wallet, summary = info.summary, fingerprint = current?.fingerprint)
            }
            is RetrievedPmInfo.Ach -> {
                val current = this.info as? AchInfo
                AchInfo(bankName = info.bankName, last4 = info.last4, mandateId = current?.mandateId ?: info.mandateId ?: "", verification = info.verification)
            }
        }
        return copy(info = newInfo).touch(clock)
    }

    fun markDefault(clock: Clock = Clock.systemUTC()) =
        copy(isDefault = true, updatedAt = Instant.now(clock))
}