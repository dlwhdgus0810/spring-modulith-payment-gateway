package me.hyunlee.user.domain.port.inbound.commands

import me.hyunlee.laundry.user.domain.model.UserId

data class UpdateUserProfileCommand(
    val userId: UserId,
    val email: String?,
    val firstName: String?,
    val lastName: String?
)