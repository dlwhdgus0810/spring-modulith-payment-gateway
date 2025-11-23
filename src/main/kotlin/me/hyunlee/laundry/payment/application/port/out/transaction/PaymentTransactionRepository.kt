package me.hyunlee.laundry.payment.application.port.out.transaction

import me.hyunlee.laundry.payment.domain.model.transaction.PaymentTransaction
import me.hyunlee.laundry.payment.domain.model.transaction.PaymentTransactionStatus
import java.util.*

/**
 * 결제 처리 트랜잭션 영속 포트
 */
interface PaymentTransactionRepository {
    fun save(tx: PaymentTransaction): PaymentTransaction
    fun findById(id: UUID): PaymentTransaction?
    fun findByOrderId(orderId: UUID): PaymentTransaction?
    /** 동시성 제어용: orderId 기준으로 쓰기 락을 획득합니다. */
    fun findByOrderIdForUpdate(orderId: UUID): PaymentTransaction?
    fun findByPaymentIntentId(paymentIntentId: String): PaymentTransaction?

    fun updateStatusByPiId(paymentIntentId: String, status: PaymentTransactionStatus): Int
    fun updateCapturedByPiId(paymentIntentId: String, amountCaptured: Long, status: PaymentTransactionStatus = PaymentTransactionStatus.CAPTURED): Int

    fun updateRefundedByPiId(
        paymentIntentId: String,
        amountRefunded: Long,
    ): Int
}