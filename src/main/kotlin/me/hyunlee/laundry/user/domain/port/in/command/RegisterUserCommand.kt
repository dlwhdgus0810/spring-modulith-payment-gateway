package me.hyunlee.user.domain.port.inbound.commands

data class RegisterUserCommand(
    val phone: String,
    val email: String?,
    val firstName: String,
    val lastName: String,
) {
}