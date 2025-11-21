package me.hyunlee.laundry.user.adapter.out.query

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.common.domain.event.payment.UserQueryPort
import me.hyunlee.laundry.user.application.port.out.UserRepository
import org.springframework.stereotype.Component

@Component
class UserQueryAdapter(
    private val repo: UserRepository
) : UserQueryPort {
    override fun findCustomerId(userId: UserId): String? {
        return repo.findById(userId)?.customerId
    }
}