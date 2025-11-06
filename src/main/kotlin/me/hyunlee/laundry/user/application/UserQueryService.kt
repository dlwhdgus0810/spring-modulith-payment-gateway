package me.hyunlee.laundry.user.application

import me.hyunlee.laundry.user.domain.exception.UserNotFoundException
import me.hyunlee.laundry.user.domain.model.User
import me.hyunlee.laundry.user.domain.model.UserId
import me.hyunlee.laundry.user.domain.port.out.UserRepository
import me.hyunlee.user.domain.port.inbound.UserQueryUseCase
import org.springframework.stereotype.Service

@Service
class UserQueryService(
    private val repo : UserRepository,
) : UserQueryUseCase {

    override fun getById(id: UserId): User =
        repo.findById(id) ?: throw UserNotFoundException("User not found: ${id.value}")

    override fun getByCustomerId(customerId: String): User =
        repo.findByCustomerId(customerId) ?: throw UserNotFoundException("User not found: $customerId")

    override fun getByPhone(phone: String): User =
        repo.findByPhone(phone) ?: throw UserNotFoundException("User not found, phone: $phone")

}