package me.hyunlee.laundry.user.application.port.`in`

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.user.domain.model.User

interface UserReadPort {
    fun getAll(): List<User>;
    fun getById(id: UserId): User
    fun getByCustomerId(customerId: String): User
    fun getByPhone(phone: String): User
    fun ensurePhoneAvailable(phone: String)
}