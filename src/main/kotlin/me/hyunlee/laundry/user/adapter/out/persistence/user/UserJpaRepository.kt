package me.hyunlee.laundry.user.adapter.out.persistence.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserEntity, UUID> {
    fun findByPhone(phone: String): UserEntity?
    fun findByCustomerId(customerId: String): UserEntity?

    /**
     * users.customer_id 가 아직 비어있는 경우에만 원자적으로 설정합니다.
     * - 동시 이벤트/요청이 경쟁할 때도 최초 1회만 성공하도록 보장합니다.
     * - 반환값: 업데이트된 행 수(0 또는 1)
     */
    @Modifying
    @Query("update UserEntity u set u.customerId = :customerId where u.id = :userId and u.customerId is null")
    fun linkCustomerIfAbsent(
        @Param("userId") userId: UUID,
        @Param("customerId") customerId: String
    ): Int
}