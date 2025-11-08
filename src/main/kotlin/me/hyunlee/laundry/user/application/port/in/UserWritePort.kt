package me.hyunlee.laundry.user.application.port.`in`

import me.hyunlee.laundry.user.domain.model.User

interface UserWritePort {
    fun register(command: RegisterUserCommand): User
    fun updateProfile(command: UpdateUserProfileCommand): User
    fun addAddress(command: AddAddressCommand): User
}
