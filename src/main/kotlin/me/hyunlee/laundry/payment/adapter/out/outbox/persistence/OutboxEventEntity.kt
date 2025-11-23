package me.hyunlee.laundry.payment.adapter.out.outbox.persistence

import jakarta.persistence.*
import me.hyunlee.laundry.common.adapter.out.persistence.BaseEntity
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(
    name = "outbox_events",
    indexes = [
        Index(name = "ix_outbox_status_created", columnList = "status, created_at"),
        Index(name = "ix_outbox_aggregate", columnList = "aggregate_type, aggregate_id")
    ]
)
class OutboxEventEntity(
    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    var id: UUID = UUID.randomUUID(),

    @Column(name = "aggregate_type", nullable = false, length = 64)
    var aggregateType: String,

    @Column(name = "aggregate_id", nullable = false, length = 64)
    var aggregateId: String,

    @Column(name = "type", nullable = false, length = 64)
    var type: String,

    @Column(name = "payload", columnDefinition = "json", nullable = false)
    var payload: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    var status: OutboxStatus = OutboxStatus.PENDING,

    @Column(name = "attempts", nullable = false)
    var attempts: Int = 0,

    @Column(name = "processed_at")
    var processedAt: OffsetDateTime? = null,
): BaseEntity()

enum class OutboxStatus { PENDING, DONE, FAILED }
