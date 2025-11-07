package me.hyunlee.laundry.payment.adapter.out.persistence

import jakarta.persistence.*
import me.hyunlee.laundry.common.BaseEntity
import me.hyunlee.laundry.payment.adapter.out.persistence.embeddable.PaymentMethodSummaryEmbeddable
import me.hyunlee.laundry.payment.domain.model.PaymentMethodStatus
import java.util.*

@Entity
@Table(name = "payment_methods")
class PaymentMethodEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    var id: UUID? = null,

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    var userId: UUID,

    @Column(name = "provider_pm_id", nullable = false, length = 64)
    var providerPaymentMethodId: String,

    @Embedded
    var summary: PaymentMethodSummaryEmbeddable,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    var status: PaymentMethodStatus = PaymentMethodStatus.ACTIVE,

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false,
) : BaseEntity()