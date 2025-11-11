package me.hyunlee.laundry.payment.adapter.out.persistence.embeddable

import jakarta.persistence.*
import me.hyunlee.laundry.payment.domain.model.AchVerificationStatus
import me.hyunlee.laundry.payment.domain.model.PaymentMethodType
import me.hyunlee.laundry.payment.domain.model.WalletType

@Embeddable
data class PaymentMethodSummaryEmbeddable(
    @Enumerated(EnumType.STRING)
    @Column(name = "pm_summary_type", nullable = false)
    val type: PaymentMethodType,

    @Embedded
    var brand: PaymentBrandEmbeddable? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet")
    var wallet: WalletType? = null,

    @Column(name = "fingerprint", length = 64)
    var fingerprint: String? = null,

    @Column(name = "nickname", length = 64)
    var nickname: String? = null,

    // ACH 전용
    @Column(name = "bank_name", length = 64)
    var bankName: String? = null,

    @Column(name = "mandate_id", length = 64)
    var mandateId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "ach_verification")
    var verification: AchVerificationStatus? = null
)