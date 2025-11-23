package me.hyunlee.laundry.payment.adapter.out.transaction

import me.hyunlee.laundry.payment.adapter.out.transaction.persistence.PaymentTransactionJpaRepository
import me.hyunlee.laundry.payment.adapter.out.transaction.persistence.toDomain
import me.hyunlee.laundry.payment.adapter.out.transaction.persistence.toEntity
import me.hyunlee.laundry.payment.application.port.out.transaction.PaymentTransactionRepository
import me.hyunlee.laundry.payment.domain.model.transaction.PaymentTransaction
import me.hyunlee.laundry.payment.domain.model.transaction.PaymentTransactionStatus
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional(readOnly = true)
class PaymentTransactionPersistenceAdapter(
    private val jpa: PaymentTransactionJpaRepository
) : PaymentTransactionRepository {

    @Transactional
    override fun save(tx: PaymentTransaction): PaymentTransaction {
        return jpa.save(tx.toEntity()).toDomain()
    }

    override fun findById(id: UUID): PaymentTransaction? =
        jpa.findById(id).map { it.toDomain() }.orElse(null)

    override fun findByOrderId(orderId: UUID): PaymentTransaction? =
        jpa.findFirstByOrderId(orderId)?.toDomain()

    override fun findByOrderIdForUpdate(orderId: UUID): PaymentTransaction? =
        jpa.findByOrderIdForUpdate(orderId)?.toDomain()

    override fun findByPaymentIntentId(paymentIntentId: String): PaymentTransaction? =
        jpa.findFirstByPaymentIntentId(paymentIntentId)?.toDomain()

    @Transactional
    override fun updateStatusByPiId(
        paymentIntentId: String,
        status: PaymentTransactionStatus,
    ): Int = jpa.updateStatusByPiId(paymentIntentId, status)

    @Transactional
    override fun updateCapturedByPiId(
        paymentIntentId: String,
        amountCaptured: Long,
        status: PaymentTransactionStatus
    ): Int = jpa.updateCapturedByPiId(paymentIntentId, amountCaptured, status)

    @Transactional
    override fun updateRefundedByPiId(
        paymentIntentId: String,
        amountRefunded: Long,
    ): Int = jpa.updateRefundedByPiId(paymentIntentId, amountRefunded)

    // expiresAt 관련 조회는 Mongo 이벤트로 대체되므로 제거
}