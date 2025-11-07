package me.hyunlee.laundry.payment.adapter.out.persistence.embeddable

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class PaymentBrandEmbeddable(
    @Column(name = "brand", length = 32)
    var brand: String? = null,

    @Column(name = "last4", length = 4)
    var last4: String? = null,

    @Column(name = "exp_month")
    var expMonth: Int? = null,

    @Column(name = "exp_year")
    var expYear: Int? = null,
)