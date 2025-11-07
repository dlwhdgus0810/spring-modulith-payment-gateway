package me.hyunlee.user.domain.port.inbound

import me.hyunlee.laundry.common.UserId
import me.hyunlee.laundry.user.domain.model.User

interface UserQueryUseCase {
    fun getById(id: UserId): User
    fun getByCustomerId(customerId: String): User
    fun getByPhone(phone: String): User
}