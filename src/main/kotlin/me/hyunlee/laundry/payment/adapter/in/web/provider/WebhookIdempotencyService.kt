package me.hyunlee.laundry.payment.adapter.`in`.web.provider

import me.hyunlee.laundry.payment.adapter.out.provider.webhook.persistence.WebhookEventEntity
import me.hyunlee.laundry.payment.adapter.out.provider.webhook.persistence.WebhookEventJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class WebhookIdempotencyService(
    private val repo: WebhookEventJpaRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 시작 시점에 이벤트 레코드를 생성하거나 기존 것을 반환합니다.
     * 이미 PROCESSED 상태면 null을 반환하여 상위에서 단락(return)할 수 있게 합니다.
     */
    @Transactional
    fun start(eventId: String, type: String): WebhookEventEntity? {
        val existing = repo.findByEventId(eventId)
        if (existing != null && existing.status == "PROCESSED") {
            log.info("[StripeWebhook] duplicate event id={} type={}", mask(eventId), type)
            return null
        }
        return existing ?: repo.save(WebhookEventEntity(eventId = eventId, type = type))
    }

    @Transactional
    fun markProcessed(record: WebhookEventEntity) {
        record.status = "PROCESSED"
        record.processedAt = OffsetDateTime.now()
        repo.save(record)
    }

    @Transactional
    fun markFailed(record: WebhookEventEntity) {
        record.status = "FAILED"
        repo.save(record)
    }

    private fun mask(id: String?, keep: Int = 6): String? = id?.let { if (it.length <= keep) "***" else it.take(keep) + "***" }
}
