package me.hyunlee.laundry.payment.adapter.out.persistence

import me.hyunlee.laundry.common.PaymentMethodId
import me.hyunlee.laundry.common.UserId
import me.hyunlee.laundry.payment.adapter.out.persistence.embeddable.PaymentBrandEmbeddable
import me.hyunlee.laundry.payment.adapter.out.persistence.embeddable.PaymentMethodSummaryEmbeddable
import me.hyunlee.laundry.payment.domain.model.*

fun PaymentMethod.toEntity() : PaymentMethodEntity {
    return PaymentMethodEntity(
        userId = userId.value,
        providerPaymentMethodId = providerPaymentMethodId,
        summary = summary.toEntity(),
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
        providerPaymentMethodId = providerPaymentMethodId,
        summary = summary.toDomain(),
        status = status,
        isDefault = isDefault,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}


fun PaymentMethodSummary.toEntity(): PaymentMethodSummaryEmbeddable =
    when (this) {
        is CardOrWalletSummary -> PaymentMethodSummaryEmbeddable(
            type = PaymentMethodSummaryType.CARD_OR_WALLET,
            brand = this.brand?.toEntity(),
            wallet = this.wallet,
            fingerprint = this.fingerprint,
            nickname = this.nickname,
            // ACH 전용 필드는 비움
            bankName = null,
            mandateId = null,
            verification = null
        )

        is AchSummary -> {
            require(this.bankName.isNotBlank()) { "bankName is required for ACH" }
            require(this.mandateId.isNotBlank()) { "mandateId is required for ACH" }

            PaymentMethodSummaryEmbeddable(
                type = PaymentMethodSummaryType.ACH,
                // 카드 last4를 ACH에도 노출하고 싶다면 cardBrand에 last4만 채워 재사용 가능
                brand = PaymentBrandEmbeddable(last4 = this.last4),
                wallet = null,            // ACH에는 의미 없음
                fingerprint = null,       // ACH에는 의미 없음
                nickname = this.nickname,
                bankName = this.bankName,
                mandateId = this.mandateId,
                verification = this.verification
            )
        }
    }

fun PaymentMethodSummaryEmbeddable.toDomain(): PaymentMethodSummary = when (type) {
    PaymentMethodSummaryType.CARD_OR_WALLET -> CardOrWalletSummary(
        brand = brand?.toDomain(),
        wallet = wallet ?: WalletType.NONE,
        fingerprint = fingerprint,
        nickname = nickname
    )
    PaymentMethodSummaryType.ACH -> {
        require(!bankName.isNullOrBlank()) { "bankName is required for ACH" }
        require(!mandateId.isNullOrBlank()) { "mandateId is required for ACH" }
        AchSummary(
            bankName = bankName!!,
            last4 = brand?.last4,               // ← 필요하면 카드의 last4 재활용 가능
            mandateId = mandateId!!,
            verification = verification ?: AchVerificationStatus.PENDING,
            nickname = nickname
        )
    }
}

fun PaymentBrand.toEntity() : PaymentBrandEmbeddable {
    return PaymentBrandEmbeddable(
        brand = brand,
        last4 = last4,
        expMonth = expMonth,
        expYear = expYear
    )
}

fun PaymentBrandEmbeddable.toDomain() : PaymentBrand {
    return PaymentBrand(
        brand = brand,
        last4 = last4,
        expMonth = expMonth,
        expYear = expYear
    )
}