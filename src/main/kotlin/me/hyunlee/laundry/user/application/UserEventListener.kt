package me.hyunlee.laundry.user.application

import me.hyunlee.laundry.common.domain.event.payment.ProviderCustomerEnsuredEvent
import me.hyunlee.laundry.user.application.port.out.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserEventListener(
    private val userRepository: UserRepository,
) {
    private val log = LoggerFactory.getLogger(UserEventListener::class.java)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onCustomerEnsured(event: ProviderCustomerEnsuredEvent) {
        // 원자적 링크 시도 (customer_id IS NULL 인 경우에만)
        val updated = userRepository.linkCustomerIfAbsent(event.userId, event.customerId)
        if (updated) {
            log.info("[StripeCustomerLink] linked user={} to customerId={}", event.userId, event.customerId)
            return
        }

        // 실패 시 현재 상태 확인하여 멱등/충돌 구분 로그
        val current = userRepository.findById(event.userId)
        if (current == null) {
            log.warn("[StripeCustomerLink] user not found after link attempt: {} → skip", event.userId)
            return
        }
        if (current.customerId == event.customerId) {
            log.info("[StripeCustomerLink] already linked (idempotent), skip user={}", event.userId)
            return
        }
        log.warn(
            "[StripeCustomerLink] link skipped: user={} currentCustomerId={} newCustomerId={}",
            event.userId, current.customerId, event.customerId
        )
    }

}