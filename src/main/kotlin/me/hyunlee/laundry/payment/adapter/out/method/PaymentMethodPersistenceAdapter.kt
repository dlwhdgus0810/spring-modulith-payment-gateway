package me.hyunlee.laundry.payment.adapter.out.method

import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.payment.adapter.out.method.persistence.PaymentMethodJpaRepository
import me.hyunlee.laundry.payment.adapter.out.method.persistence.toDomain
import me.hyunlee.laundry.payment.adapter.out.method.persistence.toEntity
import me.hyunlee.laundry.payment.application.port.out.method.PaymentMethodRepository
import me.hyunlee.laundry.payment.domain.model.method.PaymentMethod
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = true)
class PaymentMethodPersistenceAdapter(
    private val jpa : PaymentMethodJpaRepository
) : PaymentMethodRepository {

    @Transactional
    override fun save(pm: PaymentMethod): PaymentMethod {
        return jpa.save(pm.toEntity()).toDomain()
    }

    override fun findById(id: PaymentMethodId): PaymentMethod? {
        return jpa.findById(id.value).map { it.toDomain() }.orElse(null)
    }

    override fun findByUser(userId: UserId): List<PaymentMethod> {
        return jpa.findByUserId(userId.value).map { it.toDomain() }
    }

    override fun findDefaultByUser(userId: UserId): PaymentMethod? {
        return jpa.findFirstByUserIdAndIsDefaultTrue(userId.value)?.toDomain()
    }

    override fun existsByUserAndProviderPmId(userId: UserId, providerPaymentMethodId: String): Boolean {
        return jpa.existsByUserIdAndProviderPaymentMethodId(userId.value, providerPaymentMethodId)
    }

    override fun findByUserAndProviderPmId(userId: UserId, providerPaymentMethodId: String): PaymentMethod? {
        return jpa.findFirstByUserIdAndProviderPaymentMethodId(userId.value, providerPaymentMethodId)?.toDomain()
    }

    override fun findByProviderPmId(providerPaymentMethodId: String): PaymentMethod? {
        return jpa.findFirstByProviderPaymentMethodId(providerPaymentMethodId)?.toDomain()
    }

    override fun findByUserAndFingerprint(userId: UserId, fingerprint: String): PaymentMethod? {
        return jpa.findFirstByUserIdAndSummary_Fingerprint(userId.value, fingerprint)?.toDomain()
    }

    @Transactional
    override fun unsetDefaultForUser(userId: UserId): Int {
        return jpa.unsetDefaultForUser(userId.value)
    }

    @Transactional
    override fun setDefaultForUser(userId: UserId, paymentMethodId: PaymentMethodId): Int {
        return jpa.setDefaultForUser(userId.value, paymentMethodId.value)
    }

    @Transactional
    override fun unsetDefaultForUser(userId: UserId, paymentMethodId: PaymentMethodId): Int {
        return jpa.unsetDefaultForUser(userId.value, paymentMethodId.value)
    }

    @Transactional
    override fun makeDefault(userId: UserId, paymentMethodId: PaymentMethodId): Int {
        return jpa.makeDefault(userId.value, paymentMethodId.value)
    }
}