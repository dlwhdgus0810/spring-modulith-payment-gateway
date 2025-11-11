package me.hyunlee.laundry.user.application

import me.hyunlee.laundry.common.domain.event.payment.ProviderCustomerEnsuredEvent
import me.hyunlee.laundry.user.application.port.out.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserEventListener(
    private val userRepository: UserRepository,
) {
    private val log = LoggerFactory.getLogger(UserEventListener::class.java)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onCustomerEnsured(event: ProviderCustomerEnsuredEvent) {
        val user = userRepository.findById(event.userId) ?: run {
            log.warn("[StripeCustomerLink] user not found: {} → skip", event.userId)
            return
        }
        if (user.customerId == event.customerId) return // 멱등
        if (user.customerId != null) {
            log.warn("[StripeCustomerLink] user {} has different customerId(old={}, new={})", user.id, user.customerId, event.customerId)
            return // 정책적으로 덮지 않음(필요 시 허용 정책으로 변경)
        }
        userRepository.save(user.copy(customerId = event.customerId))
        log.info("[StripeCustomerLink] linked user={} to customerId={}", user.id, event.customerId)
    }

}