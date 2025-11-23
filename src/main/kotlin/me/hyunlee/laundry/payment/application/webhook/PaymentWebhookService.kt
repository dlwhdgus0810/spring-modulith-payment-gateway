package me.hyunlee.laundry.payment.application.webhook

import me.hyunlee.laundry.payment.application.port.`in`.method.PaymentMethodProviderPort
import me.hyunlee.laundry.payment.application.port.`in`.method.RetrievedPmInfo
import me.hyunlee.laundry.payment.application.port.out.method.PaymentMethodRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Webhook-driven application service for Stripe events.
 * Note: To respect module boundaries (no user lookup from payment), this service operates best-effort
 * using provider pm_id only. When a PaymentMethod is already known, it refreshes or detaches.
 * For finalize-by-webhook when PM is unknown (no user context), it logs and skips.
 */
@Service
class PaymentWebhookService(
    private val repo: PaymentMethodRepository,
    private val provider: PaymentMethodProviderPort,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Safety net for setup_intent.succeeded. If the PM already exists for some user, no-op.
     * If not found, we cannot safely create it without user context. Log and skip (reconcile via client finalize or future job).
     */
    @Transactional
    fun finalizeByWebhook(customerId: String, pmId: String) {
        repo.findByProviderPmId(pmId)?.let {
            log.info("[Webhook] finalize skip: pm already exists user={} pm={}", it.userId, pmId)
            return
        }
        log.warn("[Webhook] finalize skipped: unknown pm={}, customerId={}. Waiting for client finalize or reconciliation.", pmId, customerId)
    }

    /**
     * Refresh snapshot for known PMs when Stripe reports automatic updates.
     */
    @Transactional
    fun refreshPaymentMethodSnapshot(customerId: String, pmId: String) {
        val pm = repo.findByProviderPmId(pmId) ?: run {
            log.warn("[Webhook] refresh skip: pm not found pm={} customerId={}", pmId, customerId)
            return
        }
        val info: RetrievedPmInfo = provider.retrievePaymentMethodInfo(pmId)
        val updated = pm.refreshFrom(info)
        repo.save(updated)
        log.info("[Webhook] refreshed snapshot for pm={} user={}", pmId, pm.userId)
    }

    /**
     * Mark the PM as detached if known.
     */
    @Transactional
    fun markDetached(customerId: String?, pmId: String) {
        val pm = repo.findByProviderPmId(pmId) ?: run {
            log.warn("[Webhook] detach skip: pm not found pm={} customerId={}", pmId, customerId)
            return
        }
        val updated = pm.markDetached()
        repo.save(updated)
        log.info("[Webhook] marked detached pm={} user={}", pmId, pm.userId)
    }

    // 결제수단 관련 웹훅에만 집중 (payment_intent.* 분리는 PaymentProcessingService로 이동)
}