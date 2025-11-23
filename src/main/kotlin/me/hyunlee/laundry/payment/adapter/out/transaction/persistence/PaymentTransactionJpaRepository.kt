package me.hyunlee.laundry.payment.adapter.out.transaction.persistence

import jakarta.persistence.LockModeType
import me.hyunlee.laundry.payment.domain.model.transaction.PaymentTransactionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.*

interface PaymentTransactionJpaRepository : JpaRepository<PaymentTransactionEntity, UUID> {

    fun findFirstByOrderId(orderId: UUID): PaymentTransactionEntity?

    fun findFirstByPaymentIntentId(paymentIntentId: String): PaymentTransactionEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentTransactionEntity p where p.orderId = :orderId")
    fun findByOrderIdForUpdate(@Param("orderId") orderId: UUID): PaymentTransactionEntity?

    @Modifying
    @Query("UPDATE PaymentTransactionEntity p SET p.status = :status WHERE p.paymentIntentId = :piId")
    fun updateStatusByPiId(
        @Param("piId") paymentIntentId: String,
        @Param("status") status: PaymentTransactionStatus,
    ): Int

    @Modifying
    @Query(
        """
    UPDATE PaymentTransactionEntity p
    SET 
        p.amountCaptured = :amount,
        p.status = :status,
        p.capturedAt = :capturedAt
    WHERE p.paymentIntentId = :piId
    """
    )
    fun updateCapturedByPiId(
        @Param("piId") paymentIntentId: String,
        @Param("amount") amountCaptured: Long,
        @Param("status") status: PaymentTransactionStatus = PaymentTransactionStatus.CAPTURED,
        @Param("capturedAt") capturedAt: OffsetDateTime = OffsetDateTime.now()
    ): Int

    @Modifying
    @Query(
        "UPDATE PaymentTransactionEntity p SET p.amountRefunded = :amountRefunded, p.refundedLastAt = :refundedAt WHERE p.paymentIntentId = :piId"
    )
    fun updateRefundedByPiId(
        @Param("piId") paymentIntentId: String,
        @Param("amountRefunded") amountRefunded: Long,
        @Param("refundedAt") refundedAt: OffsetDateTime = OffsetDateTime.now()
    ): Int
}
