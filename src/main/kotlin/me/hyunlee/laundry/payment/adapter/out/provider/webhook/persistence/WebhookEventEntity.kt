package me.hyunlee.laundry.payment.adapter.out.provider.webhook.persistence

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "webhook_events",
    uniqueConstraints = [UniqueConstraint(name = "uk_webhook_event_id", columnNames = ["eventId"])]
)
class WebhookEventEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, length = 64)
    var eventId: String,

    @Column(nullable = false, length = 128)
    var type: String,

    @Column(nullable = false)
    var receivedAt: OffsetDateTime = OffsetDateTime.now(),

    var processedAt: OffsetDateTime? = null,

    @Column(nullable = false, length = 32)
    var status: String = "RECEIVED"
)
