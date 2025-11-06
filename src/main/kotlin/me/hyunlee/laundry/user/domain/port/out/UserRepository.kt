package me.hyunlee.laundry.user.domain.port.out

import me.hyunlee.laundry.user.domain.model.User
import me.hyunlee.laundry.user.domain.model.UserId

interface UserRepository {
    fun findById(id: UserId): User?
    fun findByCustomerId(customerId: String): User?
    fun findByPhone(phone: String): User?
    fun save(user: User): User
    fun existsById(id: UserId): Boolean
}