package me.hyunlee.laundry.user.domain.model

data class Address(
    val id: Long? = null,
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val secondary: String? = null,
    val instructions: String? = null,
    val isPrimary: Boolean = false
)