package me.hyunlee.laundry.payment.adapter.out.provider.webhook.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface WebhookEventJpaRepository : JpaRepository<WebhookEventEntity, Long> {
    fun findByEventId(eventId: String): WebhookEventEntity?
}