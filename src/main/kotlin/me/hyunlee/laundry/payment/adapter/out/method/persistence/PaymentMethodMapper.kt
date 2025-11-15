package me.hyunlee.laundry.payment.adapter.out.method.persistence

import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.payment.adapter.out.method.persistence.embeddable.PaymentBrandEmbeddable
import me.hyunlee.laundry.payment.adapter.out.method.persistence.embeddable.PaymentMethodSummaryEmbeddable
import me.hyunlee.laundry.payment.domain.model.method.AchInfo
import me.hyunlee.laundry.payment.domain.model.method.AchVerificationStatus
import me.hyunlee.laundry.payment.domain.model.method.CardInfo
import me.hyunlee.laundry.payment.domain.model.method.PaymentInfo
import me.hyunlee.laundry.payment.domain.model.method.PaymentMethod
import me.hyunlee.laundry.payment.domain.model.method.PaymentMethodType
import me.hyunlee.laundry.payment.domain.model.method.PaymentSummary
import me.hyunlee.laundry.payment.domain.model.method.WalletInfo

fun PaymentMethod.toEntity(): PaymentMethodEntity {
    return PaymentMethodEntity(
        userId = userId.value,
        providerPaymentMethodId = providerPmId,
        summary = info.toEntity(nickname), // ← PM.nickname을 Embeddable에 전달
        status = status,
        isDefault = isDefault
    )
}

fun PaymentMethodEntity.toDomain() : PaymentMethod {

    val pmId = requireNotNull(id) { "PaymentMethodEntity.id must not be null when converting to domain." }
    val userId = requireNotNull(userId) { "PaymentMethodEntity.userId must not be null when converting to domain." }

    return PaymentMethod(
        id = PaymentMethodId(pmId),
        userId = UserId(userId),
        providerPmId = providerPaymentMethodId,
        info = summary.toDomain(),
        status = status,
        isDefault = isDefault,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}


fun PaymentInfo.toEntity(nickname: String? = null): PaymentMethodSummaryEmbeddable = when (this) {
    is CardInfo -> PaymentMethodSummaryEmbeddable(
        type = PaymentMethodType.CARD,
        brand = this.summary.toEntity(),
        wallet = null,                      // 카드에는 지갑 없음
        fingerprint = this.fingerprint,
        nickname = nickname,
        bankName = null,
        mandateId = null,
        verification = null
    )

    is WalletInfo -> PaymentMethodSummaryEmbeddable(
        type = PaymentMethodType.WALLET,
        brand = this.summary?.toEntity(),    // 일부 월렛은 브랜드/last4 없음
        wallet = this.wallet,
        fingerprint = this.fingerprint,
        nickname = nickname,
        bankName = null,
        mandateId = null,
        verification = null
    )

    is AchInfo -> PaymentMethodSummaryEmbeddable(
        type = PaymentMethodType.ACH,
        brand = PaymentBrandEmbeddable(last4 = this.last4),
        wallet = null,
        fingerprint = null,
        nickname = nickname,
        bankName = this.bankName,
        mandateId = this.mandateId,
        verification = this.verification
    )
}

fun PaymentMethodSummaryEmbeddable.toDomain(): PaymentInfo = when (type) {
    PaymentMethodType.CARD -> {
        val summary = requireNotNull(brand) { "brand required for CARD" }.toDomain()
        CardInfo(
            summary = summary,
            fingerprint = fingerprint
        )
    }

    PaymentMethodType.WALLET -> {
        val w = requireNotNull(wallet) { "wallet required for WALLET" }
        WalletInfo(
            wallet = w,
            summary = brand?.toDomain(),
            fingerprint = fingerprint
        )
    }

    PaymentMethodType.ACH -> {
        require(!bankName.isNullOrBlank()) { "bankName is required for ACH" }
        require(!mandateId.isNullOrBlank()) { "mandateId is required for ACH" }
        AchInfo(
            bankName = bankName!!,
            last4 = brand?.last4,
            mandateId = mandateId!!,
            verification = verification ?: AchVerificationStatus.PENDING
        )
    }
}

fun PaymentSummary.toEntity() : PaymentBrandEmbeddable {
    return PaymentBrandEmbeddable(
        brand = brand,
        last4 = last4,
        expMonth = expMonth,
        expYear = expYear
    )
}

fun PaymentBrandEmbeddable.toDomain() : PaymentSummary {
    return PaymentSummary(
        brand = brand,
        last4 = last4,
        expMonth = expMonth,
        expYear = expYear
    )
}