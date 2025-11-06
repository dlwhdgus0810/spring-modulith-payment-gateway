package me.hyunlee.laundry.user.domain.model

import java.util.UUID

@JvmInline
value class AddressId(val value: UUID) {
    override fun toString(): String = value.toString()
}

data class Address(
    val id: AddressId,
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val secondary: String? = null,
    val instructions: String? = null,
    val isPrimary: Boolean = false
) {
    companion object {
        fun create() : Address = Address(
            id = AddressId(UUID.randomUUID()),
            street = "",
            city = "",
            state = "",
            postalCode = "",
            secondary = null,
            instructions = null,
            isPrimary = false
        )
    }
}