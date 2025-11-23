package me.hyunlee.laundry.payment.adapter.out.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import me.hyunlee.laundry.payment.adapter.out.outbox.persistence.OutboxEventEntity
import me.hyunlee.laundry.payment.adapter.out.outbox.persistence.OutboxEventJpaRepository
import me.hyunlee.laundry.payment.adapter.out.outbox.persistence.OutboxStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 트랜잭션 경계 내에서 Outbox 이벤트를 적재하는 유틸리티.
 * - aggregateType 예: "PaymentTransaction"
 * - type 예: AUTHORIZED | REQUIRES_CAPTURE | CAPTURED | FAILED | CANCELED | REFUND_CREATED | REFUND_UPDATED
 */
@Component
class OutboxWriter(
    private val repo: OutboxEventJpaRepository,
    private val om: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun append(
        aggregateType: String,
        aggregateId: String,
        type: String,
        payload: Map<String, Any?>
    ): UUID {
        val json = om.writeValueAsString(payload)
        val entity = OutboxEventEntity(
            id = UUID.randomUUID(),
            aggregateType = aggregateType,
            aggregateId = aggregateId,
            type = type,
            payload = json,
            status = OutboxStatus.PENDING,
            attempts = 0,
            processedAt = null,
        )
        repo.save(entity)
        log.debug("[Outbox] appended id={} type={} agg={} payload={}", entity.id, type, aggregateId, json)
        return entity.id
    }
}
