package me.hyunlee.laundry.common.domain.event.payment

import me.hyunlee.laundry.common.domain.UserId

interface UserQueryPort {
    fun findCustomerId(userId: UserId): String?
}