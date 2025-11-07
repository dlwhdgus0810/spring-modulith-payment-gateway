package me.hyunlee.laundry.order.adapter.out.persistence.order

import jakarta.persistence.*
import me.hyunlee.laundry.order.domain.model.vo.PaymentStatus
import me.hyunlee.order.domain.model.enums.TipSelection
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Embeddable
data class PaymentInfoEmbeddable(
    @Column(name = "pm_id", columnDefinition = "BINARY(16)", nullable = false)
    var id: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 16, nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,

    @Embedded
    var snapshot: PaymentSnapshotEmbeddable? = null
)

@Embeddable
data class PaymentSnapshotEmbeddable(
    var brand: String?,
    var last4: String?,
    var expMonth: Int?,
    var expYear: Int?,
    var nickname: String?
)

@Embeddable
data class ContactEmbeddable(
    var phone: String,
    var email: String?,
    @Embedded
    var address: AddressEmbeddable
)

@Embeddable
data class AddressEmbeddable(
    @Column(nullable = false) var street: String,
    @Column(nullable = false) var city: String,
    @Column(nullable = false) var state: String,
    @Column(nullable = false) var postalCode: String,
    var secondary: String? = null,
    var instructions: String? = null
)

@Embeddable
data class ScheduleEmbeddable(
    @Column(name = "pickup_date", nullable = false)
    var pickupDate: LocalDate,

    @Column(name = "pickup_slot_index", nullable = false)
    var pickupSlot: Int,

    @Column(name = "delivery_date")
    var deliveryDate: LocalDate? = null,

    @Column(name = "delivery_slot_index")
    var deliverySlot: Int? = null
)

@Embeddable
data class TipEmbeddable(
    @Enumerated(EnumType.STRING)
    @Column(name = "tip_type", length = 16, nullable = false)
    var type: TipSelection,

    @Column(name = "tip_custom_amount", precision = 10, scale = 2)
    var amount: BigDecimal? = null
)

