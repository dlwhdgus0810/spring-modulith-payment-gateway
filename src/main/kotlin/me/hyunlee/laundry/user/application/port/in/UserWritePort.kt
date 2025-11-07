package me.hyunlee.user.domain.port.inbound

import me.hyunlee.laundry.user.application.port.`in`.AddAddressCommand
import me.hyunlee.laundry.user.application.port.`in`.RegisterUserCommand
import me.hyunlee.laundry.user.application.port.`in`.UpdateUserProfileCommand
import me.hyunlee.laundry.user.domain.model.User

interface UserWritePort {
    fun register(command: RegisterUserCommand): User
    fun updateProfile(command: UpdateUserProfileCommand): User
    fun addAddress(command: AddAddressCommand): User
}
