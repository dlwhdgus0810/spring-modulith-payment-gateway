package me.hyunlee.laundry.user.application.port.`in`

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.user.domain.model.Address

sealed class UserCommand

data class AddAddressCommand(
    val userId: UserId,
    val address: Address
)

data class RegisterUserCommand(
    val phone: String,
    val email: String?,
    val firstName: String,
    val lastName: String,
) {
    init {
        require(phone.isNotBlank()) { "Phone number cannot be blank" }
        require(firstName.isNotBlank()) { "First name cannot be blank" }
        require(lastName.isNotBlank()) { "Last name cannot be blank" }
    }
}

data class UpdateUserProfileCommand(
    val userId: UserId,
    val email: String?,
    val firstName: String?,
    val lastName: String?
)