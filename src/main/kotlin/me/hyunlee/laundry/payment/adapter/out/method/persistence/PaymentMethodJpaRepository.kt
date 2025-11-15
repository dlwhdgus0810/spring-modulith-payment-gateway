package me.hyunlee.laundry.payment.adapter.out.method.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface PaymentMethodJpaRepository : JpaRepository<PaymentMethodEntity, UUID> {

    fun findByUserId(@Param("userId") userId: UUID): List<PaymentMethodEntity>

    fun findFirstByUserIdAndIsDefaultTrue(@Param("userId") userId: UUID): PaymentMethodEntity?

    fun existsByUserIdAndProviderPaymentMethodId(@Param("userId") userId: UUID, @Param("providerPaymentMethodId") providerPmId: String): Boolean
    fun findFirstByUserIdAndProviderPaymentMethodId(@Param("userId") userId: UUID, @Param("providerPaymentMethodId") providerPmId: String): PaymentMethodEntity?
    fun findFirstByProviderPaymentMethodId(@Param("providerPaymentMethodId") providerPmId: String): PaymentMethodEntity?

    // fingerprint is stored inside embedded summary
    fun findFirstByUserIdAndSummary_Fingerprint(@Param("userId") userId: UUID, @Param("fingerprint") fingerprint: String): PaymentMethodEntity?

    @Modifying
    @Query("update PaymentMethodEntity p set p.isDefault=false where p.userId=:userId and p.isDefault=true")
    fun unsetDefaultForUser(@Param("userId") userId: UUID): Int

    @Modifying
    @Query("update PaymentMethodEntity p set p.isDefault=true where p.id=:pmId and p.userId=:userId")
    fun setDefaultForUser(@Param("userId") userId: UUID, @Param("pmId") paymentMethodId: UUID): Int

    @Modifying
    @Query("update PaymentMethodEntity p set p.isDefault=false where p.id=:pmId and p.userId=:userId and p.isDefault=true")
    fun unsetDefaultForUser(@Param("userId") userId: UUID, @Param("pmId") paymentMethodId: UUID): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
            UPDATE payment_methods
            SET is_default = CASE WHEN id = :pmId THEN TRUE ELSE FALSE END
            WHERE user_id = :userId
        """,
        nativeQuery = true
    )
    fun makeDefault(userId: UUID, pmId: UUID): Int
}