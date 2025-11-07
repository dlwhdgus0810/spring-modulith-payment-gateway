package me.hyunlee.laundry.user.application.port.out

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.user.domain.model.User

interface UserRepository {
    fun findById(id: UserId): User?
    fun findByCustomerId(customerId: String): User?
    fun findByPhone(phone: String): User?
    fun existsById(id: UserId): Boolean
    fun save(user: User): User
}