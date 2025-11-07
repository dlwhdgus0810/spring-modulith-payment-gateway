package me.hyunlee.user.adapter.`in`.web.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import me.hyunlee.laundry.common.UserId
import me.hyunlee.laundry.user.domain.model.Address
import me.hyunlee.laundry.user.domain.port.`in`.AddAddressCommand
import me.hyunlee.laundry.user.domain.port.`in`.RegisterUserCommand
import me.hyunlee.laundry.user.domain.port.`in`.UpdateUserProfileCommand
import java.util.*

data class RegisterUserRequest(
    @field:NotBlank @field:Size(max = 32)
    val phone: String,
    @Email
    val email: String?,
    @field:NotBlank @field:Size(max = 100)
    val firstName: String,
    @field:NotBlank @field:Size(max = 100)
    val lastName: String,
) {
    fun toCommand(): RegisterUserCommand =
        RegisterUserCommand(
            phone = phone,
            email = email,
            firstName = firstName,
            lastName = lastName,
        )
}

data class UpdateUserProfileRequest(
    val email: String?,
    val firstName: String?,
    val lastName: String?
) {

    fun toCommand(userId : UUID): UpdateUserProfileCommand =
        UpdateUserProfileCommand(
            userId = UserId(userId),
            email = email,
            firstName = firstName,
            lastName = lastName
        )
}

data class AddAddressRequest(
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val secondary: String? = null,
    val instructions: String? = null,
    val isPrimary: Boolean = false
) {
    fun toCommand(userId: UUID): AddAddressCommand =
        AddAddressCommand(
            userId = UserId(userId),
            address = Address(
                street = street,
                city = city,
                state = state,
                postalCode = postalCode,
                secondary = secondary,
                instructions = instructions,
                isPrimary = isPrimary
            )
        )
}