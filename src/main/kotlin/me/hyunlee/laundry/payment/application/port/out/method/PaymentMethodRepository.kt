package me.hyunlee.laundry.payment.application.port.out.method

import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.payment.domain.model.method.PaymentMethod

interface PaymentMethodRepository {
    fun save(pm: PaymentMethod) : PaymentMethod

    fun findById(id: PaymentMethodId): PaymentMethod?

    fun findByUser(userId: UserId): List<PaymentMethod>

    fun findDefaultByUser(userId: UserId): PaymentMethod?

    fun existsByUserAndProviderPmId(userId: UserId, providerPaymentMethodId: String): Boolean
    fun findByUserAndProviderPmId(userId: UserId, providerPaymentMethodId: String): PaymentMethod?
    fun findByProviderPmId(providerPaymentMethodId: String): PaymentMethod?
    fun findByUserAndFingerprint(userId: UserId, fingerprint: String): PaymentMethod?

    /** Unset current default payment method for the user (if any). Must be in a tx. */
    fun unsetDefaultForUser(userId: UserId): Int

    /** Set target payment method as default for the user. Must be in a tx. */
    fun setDefaultForUser(userId: UserId, paymentMethodId: PaymentMethodId): Int

    /** Unset specific payment method as default (no-op if not default). Must be in a tx. */
    fun unsetDefaultForUser(userId: UserId, paymentMethodId: PaymentMethodId): Int

    fun makeDefault(userId: UserId, paymentMethodId: PaymentMethodId): Int
}