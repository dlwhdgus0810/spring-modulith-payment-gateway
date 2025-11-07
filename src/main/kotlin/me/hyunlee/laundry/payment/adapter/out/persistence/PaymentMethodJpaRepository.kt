package me.hyunlee.laundry.payment.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface PaymentMethodJpaRepository : JpaRepository<PaymentMethodEntity, UUID> {

    fun findByUserId(@Param("userId") userId: UUID): List<PaymentMethodEntity>

    fun findFirstByUserIdAndIsDefaultTrue(@Param("userId") userId: UUID): PaymentMethodEntity?

    fun existsByUserIdAndProviderPaymentMethodId(@Param("userId") userId: UUID, @Param("providerPaymentMethodId") providerPaymentMethodId: String): Boolean

    @Modifying
    @Query("update PaymentMethodEntity p set p.isDefault=false where p.userId=:userId and p.isDefault=true")
    fun unsetDefaultForUser(@Param("userId") userId: UUID): Int
}