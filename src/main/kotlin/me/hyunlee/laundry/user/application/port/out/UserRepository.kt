package me.hyunlee.laundry.user.application.port.out

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.user.domain.model.User

interface UserRepository {
    fun findById(id: UserId): User?
    fun findByCustomerId(customerId: String): User?
    fun findByPhone(phone: String): User?
    fun existsById(id: UserId): Boolean
    fun save(user: User): User

    /**
     * 사용자에 아직 customerId가 없을 때에만 원자적으로 설정합니다.
     * - 동시 이벤트에서도 최초 1회만 성공(true), 나머지는 false 반환
     */
    fun linkCustomerIfAbsent(userId: UserId, customerId: String): Boolean
}