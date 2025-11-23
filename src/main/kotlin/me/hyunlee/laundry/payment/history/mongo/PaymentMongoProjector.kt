//package me.hyunlee.laundry.payment.history.mongo
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.mongodb.DuplicateKeyException
//import io.micrometer.core.instrument.MeterRegistry
//import me.hyunlee.laundry.monitoring.adapter.out.slack.SlackNotifier
//import me.hyunlee.laundry.payment.history.mongo.doc.PaymentTransactionEventDoc
//import me.hyunlee.laundry.payment.history.mongo.doc.ProviderIds
//import me.hyunlee.laundry.payment.history.mongo.repo.PaymentTransactionEventRepository
//import me.hyunlee.laundry.payment.adapter.out.outbox.persistence.OutboxEventEntity
//import me.hyunlee.laundry.payment.adapter.out.outbox.persistence.OutboxEventJpaRepository
//import me.hyunlee.laundry.payment.adapter.out.outbox.persistence.OutboxStatus
//import org.slf4j.LoggerFactory
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
//import org.springframework.scheduling.annotation.Scheduled
//import org.springframework.stereotype.Component
//import org.springframework.transaction.annotation.Transactional
//import java.time.OffsetDateTime
//
//@Component
//@ConditionalOnProperty(prefix = "payments.mongo", name = ["enabled", "projector-enabled"], havingValue = "true")
//class PaymentMongoProjector(
//    private val props: PaymentMongoProperties,
//    private val outboxRepo: OutboxEventJpaRepository,
//    private val mongoRepo: PaymentTransactionEventRepository,
//    private val om: ObjectMapper,
//    private val meter: MeterRegistry,
//    private val slack: SlackNotifier,
//) {
//    private val log = LoggerFactory.getLogger(javaClass)
//
//    private val successCounter = meter.counter("payments.mongo.projector.success.count")
//    private val failureCounter = meter.counter("payments.mongo.projector.failure.count")
//
//    // 기본 주기: 1초 (운영에서는 프로퍼티화 권장)
//    @Scheduled(fixedDelayString = "1000")
//    fun poll() {
//        if (!props.enabled) return
//        val batch = outboxRepo.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)
//        if (batch.isEmpty()) return
//        batch.forEach { processOne(it) }
//    }
//
//    @Transactional
//    fun processOne(evt: OutboxEventEntity) {
//        try {
//            val payload: Map<String, Any?> = runCatching {
//                om.readValue(evt.payload, Map::class.java) as Map<String, Any?>
//            }.getOrElse { emptyMap() }
//
//            val masked = MaskingUtil.maskPayload(payload) as? Map<String, Any?>
//
//            val provider = ProviderIds(
//                pi = payload["pi"]?.toString() ?: payload["paymentIntentId"]?.toString(),
//                ch = payload["ch"]?.toString() ?: payload["chargeId"]?.toString(),
//                pm = payload["pm"]?.toString() ?: payload["paymentMethodId"]?.toString(),
//                cus = payload["cus"]?.toString() ?: payload["customerId"]?.toString(),
//            )
//
//            val doc = PaymentTransactionEventDoc(
//                sourceId = evt.id.toString(),
//                schemaVersion = 1,
//                occurredAt = OffsetDateTime.now(),
//                orderId = payload["orderId"]?.toString(),
//                userId = payload["userId"]?.toString(),
//                provider = provider,
//                type = evt.type,
//                amount = (payload["amount"] as? Number)?.toLong(),
//                currency = payload["currency"]?.toString(),
//                actor = payload["actor"]?.toString(),
//                reason = payload["reason"]?.toString(),
//                idempotencyKey = payload["idempotencyKey"]?.toString(),
//                webhookEventId = payload["webhookEventId"]?.toString(),
//                payload = masked
//            )
//
//            // idempotent write
//            try {
//                mongoRepo.insert(doc)
//            } catch (dupe: DuplicateKeyException) {
//                log.debug("[Projector] duplicate sourceId={} ignored", doc.sourceId)
//            }
//
//            evt.status = OutboxStatus.DONE
//            evt.processedAt = OffsetDateTime.now()
//            outboxRepo.save(evt)
//            successCounter.increment()
//        } catch (e: Exception) {
//            evt.attempts += 1
//            val maxAttempts = 5
//            if (evt.attempts >= maxAttempts) {
//                evt.status = OutboxStatus.FAILED
//            }
//            outboxRepo.save(evt)
//            failureCounter.increment()
//            log.error("[Projector] failed id={} type={} attempts={} msg={}", evt.id, evt.type, evt.attempts, e.message, e)
//            if (evt.attempts == maxAttempts) {
//                slack.notify(
//                    title = "Payment Mongo Projector Failed",
//                    message = "outbox=${evt.id} type=${evt.type} attempts=${evt.attempts}",
//                    severity = "ERROR",
//                    context = mapOf("aggregateId" to evt.aggregateId)
//                )
//            }
//        }
//    }
//}
