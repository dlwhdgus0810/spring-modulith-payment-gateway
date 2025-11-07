package me.hyunlee.user.domain.port.inbound

import me.hyunlee.laundry.user.domain.model.User
import me.hyunlee.laundry.user.domain.port.`in`.AddAddressCommand
import me.hyunlee.laundry.user.domain.port.`in`.RegisterUserCommand
import me.hyunlee.laundry.user.domain.port.`in`.UpdateUserProfileCommand

interface UserCommandUseCase {
    fun register(command: RegisterUserCommand): User
    fun updateProfile(command: UpdateUserProfileCommand): User
    fun addAddress(command: AddAddressCommand): User
}
