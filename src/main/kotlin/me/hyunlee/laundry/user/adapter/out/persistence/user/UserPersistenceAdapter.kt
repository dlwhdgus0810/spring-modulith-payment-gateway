package me.hyunlee.laundry.user.adapter.out.persistence.user

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.user.application.port.out.UserRepository
import me.hyunlee.laundry.user.domain.model.User
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = true)
class UserPersistenceAdapter(
    private val jpa: UserJpaRepository
) : UserRepository {

    @Transactional
    override fun save(user: User): User =
        jpa.save(user.toEntity()).toDomain()

    override fun findById(id: UserId): User? =
        jpa.findById(id.value).orElse(null)?.toDomain()

    override fun findByCustomerId(customerId: String): User? =
        jpa.findByCustomerId(customerId)?.toDomain()

    override fun findByPhone(phone: String): User? =
        jpa.findByPhone(phone)?.toDomain()

    override fun existsById(id: UserId): Boolean =
        jpa.existsById(id.value)

    @Transactional
    override fun linkCustomerIfAbsent(userId: UserId, customerId: String): Boolean {
        val updated = jpa.linkCustomerIfAbsent(userId.value, customerId)
        return updated > 0
    }
}