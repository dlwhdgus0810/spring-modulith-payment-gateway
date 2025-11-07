package me.hyunlee.laundry.payment.adapter.out.persistence

import me.hyunlee.laundry.common.UserId
import me.hyunlee.laundry.payment.domain.model.PaymentMethod
import me.hyunlee.laundry.payment.domain.port.out.PaymentMethodRepository
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

    override fun findByUser(userId: UserId): List<PaymentMethod> {
        return jpa.findByUserId(userId.value).map { it.toDomain() }
    }

    override fun findDefaultByUser(userId: UserId): PaymentMethod? {
        return jpa.findFirstByUserIdAndIsDefaultTrue(userId.value)?.toDomain()
    }

    override fun existsByUserAndProviderPmId(userId: UserId, providerPaymentMethodId: String): Boolean {
        return jpa.existsByUserIdAndProviderPaymentMethodId(userId.value, providerPaymentMethodId)
    }

    @Transactional
    override fun unsetDefaultForUser(userId: UserId): Int {
        return jpa.unsetDefaultForUser(userId.value)
    }
}