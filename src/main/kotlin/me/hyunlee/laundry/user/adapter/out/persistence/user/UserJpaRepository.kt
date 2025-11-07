package me.hyunlee.laundry.user.adapter.out.persistence.user

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserEntity, UUID> {
    fun findByPhone(phone: String): UserEntity?
    fun findByCustomerId(customerId: String): UserEntity?
}