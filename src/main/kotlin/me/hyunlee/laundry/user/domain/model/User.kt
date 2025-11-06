package me.hyunlee.laundry.user.domain.model

import me.hyunlee.laundry.user.domain.event.UserRegisteredEvent
import me.hyunlee.laundry.user.domain.event.UserUpdatedEvent
import java.util.*

@JvmInline
value class UserId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        fun newId(): UserId = UserId(UUID.randomUUID())
    }
}

data class User(
    val id: UserId,
    val phone: String,
    val email: String? = null,
    val firstName: String,
    val lastName: String,
    val addresses: List<Address> = emptyList(),
    val customerId: String? = null
) {

    companion object {
        fun create(phone: String, email: String?, firstName: String, lastName: String): Pair<User, UserRegisteredEvent> {
            val user = User(UserId.newId(), phone, email, firstName, lastName)

            val event = UserRegisteredEvent(
                id = user.id.toString(),
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
            id = updated.id.toString(),
            email = updated.email,
            firstName = updated.firstName,
            lastName = updated.lastName
        )

        return updated to event
    }

    fun addAddress(newAddress: Address): User {
        val updatedAddresses =
            if (newAddress.isPrimary) { addresses.map { it.copy(isPrimary = false) } + newAddress.copy(isPrimary = true) }
            else { addresses + newAddress }

        return copy(addresses = updatedAddresses)
    }
}