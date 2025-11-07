package me.hyunlee.laundry.payment.domain.port.out

import me.hyunlee.laundry.common.UserId
import me.hyunlee.laundry.payment.domain.model.PaymentMethod

interface PaymentMethodRepository {
    fun save(pm: PaymentMethod) : PaymentMethod

    fun findByUser(userId: UserId): List<PaymentMethod>

    fun findDefaultByUser(userId: UserId): PaymentMethod?

    fun existsByUserAndProviderPmId(userId: UserId, providerPaymentMethodId: String): Boolean

    /**
     * Unset current default payment method for the user (if any).
     * Should be called within a transaction.
     * @return number of rows updated
     */
    fun unsetDefaultForUser(userId: UserId): Int
}