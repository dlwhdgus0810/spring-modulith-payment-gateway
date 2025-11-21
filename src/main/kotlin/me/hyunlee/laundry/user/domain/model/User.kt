package me.hyunlee.laundry.user.domain.model

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.user.domain.event.UserRegisteredEvent
import me.hyunlee.laundry.user.domain.event.UserUpdatedEvent

enum class Role {
    ADMIN, USER
}

data class User(
    val id: UserId = UserId.newId(),
    val phone: String,
    val email: String? = null,
    val firstName: String,
    val lastName: String,
    val addresses: List<Address> = emptyList(),
    val role: Role = Role.ADMIN,
    val customerId: String? = null
) {

    companion object {
        fun create(phone: String, email: String?, firstName: String, lastName: String): Pair<User, UserRegisteredEvent> {
            val user = User(
                phone = phone,
                email = email,
                firstName = firstName,
                lastName = lastName
            )

            val event = UserRegisteredEvent(
                userId = user.id.toString(),
                phone = user.phone,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName
            )

            return user to event
        }
    }

    fun updateProfile(email: String?, firstName: String?, lastName: String?) : Pair<User, UserUpdatedEvent> {
        val updated = copy(email = email ?: this.email, firstName = firstName ?: this.firstName, lastName = lastName ?: this.lastName)

        val event = UserUpdatedEvent(
            userId = updated.id.toString(),
            email = updated.email,
            firstName = updated.firstName,
            lastName = updated.lastName
        )

        return updated to event
    }

    fun linkCustomer(newCustomerId: String): User {
        require(this.customerId == null || this.customerId == newCustomerId) { "CustomerId already set and different" }
        return if (this.customerId == newCustomerId) this else copy(customerId = newCustomerId)
    }

    fun addAddress(newAddress: Address): User {
        val updatedAddresses =
            if (newAddress.isPrimary) { addresses.map { it.copy(isPrimary = false) } + newAddress.copy(isPrimary = true) }
            else { addresses + newAddress }

        return copy(addresses = updatedAddresses)
    }
}