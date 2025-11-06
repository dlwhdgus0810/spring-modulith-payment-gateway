package me.hyunlee.user.domain.port.inbound

import me.hyunlee.laundry.user.domain.model.User
import me.hyunlee.user.domain.port.inbound.commands.AddAddressCommand
import me.hyunlee.user.domain.port.inbound.commands.RegisterUserCommand
import me.hyunlee.user.domain.port.inbound.commands.UpdateUserProfileCommand

interface UserCommandUseCase {
    fun register(command: RegisterUserCommand): User
    fun updateProfile(command: UpdateUserProfileCommand): User
    fun addAddress(command: AddAddressCommand): User
}
