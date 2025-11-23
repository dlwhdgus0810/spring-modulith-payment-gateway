package me.hyunlee.laundry.user.adapter.`in`.web.dto

import me.hyunlee.laundry.user.domain.model.Role
import me.hyunlee.laundry.user.domain.model.User

data class UserResponse(
    val id: String,
    val phone: String,
    val email: String?,
    val firstName: String,
    val lastName: String,
    val role: Role = Role.ADMIN,
)

// Mapper
fun User.toResponse(): UserResponse =
    UserResponse(
        id = this.id.toString(),
        phone = this.phone,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        role = this.role
    )