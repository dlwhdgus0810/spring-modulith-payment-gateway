package me.hyunlee.user.domain.port.inbound.commands

import me.hyunlee.laundry.user.domain.model.Address
import me.hyunlee.laundry.user.domain.model.UserId

data class AddAddressCommand(
    val userId: UserId,
    val address: Address
)