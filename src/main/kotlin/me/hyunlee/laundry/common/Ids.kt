package me.hyunlee.laundry.common

import java.util.*

@JvmInline
value class UserId(val value: UUID) {
    override fun toString(): String = value.toString()
    companion object { fun newId(): UserId = UserId(UUID.randomUUID()) }
}

@JvmInline
value class PaymentMethodId(val value: UUID) {
    override fun toString() = value.toString()
    companion object { fun newId() = PaymentMethodId(UUID.randomUUID()) }
}