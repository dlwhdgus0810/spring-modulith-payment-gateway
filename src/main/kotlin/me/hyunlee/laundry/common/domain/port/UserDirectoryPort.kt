package me.hyunlee.laundry.common.domain.port

import me.hyunlee.laundry.user.domain.model.Role
import java.util.*

interface UserDirectoryPort {

    data class UserSummary(
        val id: UUID,
        val phone: String,
        val email: String?,
        val firstName: String,
        val role: Role
    )

    fun findById(userId: UUID): UserSummary?
    fun findByPhone(phone: String): UserSummary?
    fun createByPhone(phone: String, firstName: String, lastName: String, email: String? = null): UserSummary
    fun maskedEmailOf(user: UserSummary): String?
    fun hasEmail(user: UserSummary): Boolean = maskedEmailOf(user) != null
}
