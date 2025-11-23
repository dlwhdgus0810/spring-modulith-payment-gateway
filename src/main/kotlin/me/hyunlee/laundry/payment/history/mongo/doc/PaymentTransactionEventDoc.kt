//package me.hyunlee.laundry.payment.history.mongo.doc
//
//import org.springframework.data.annotation.Id
//import org.springframework.data.mongodb.core.index.CompoundIndex
//import org.springframework.data.mongodb.core.index.CompoundIndexes
//import org.springframework.data.mongodb.core.index.Indexed
//import org.springframework.data.mongodb.core.mapping.Document
//import java.time.OffsetDateTime
//
//@Document(collection = "payment_transaction_events")
//@CompoundIndexes(
//    value = [
//        CompoundIndex(name = "ix_evt_pi_type_time", def = "{ 'provider.pi': 1, 'type': 1, 'occurredAt': -1 }")
//    ]
//)
//data class PaymentTransactionEventDoc(
//    @Id
//    val id: String? = null,
//    @Indexed(unique = true)
//    val sourceId: String,              // outbox id (UUID string) for idempotency
//    val schemaVersion: Int = 1,
//    val occurredAt: OffsetDateTime,
//    val orderId: String?,
//    val userId: String?,
//    val provider: ProviderIds,
//    val type: String,                  // AUTHORIZED | REQUIRES_CAPTURE | CAPTURED | CANCELED | FAILED | REFUND_CREATED | REFUND_UPDATED ...
//    val amount: Long?,
//    val currency: String?,
//    val actor: String?,                // OWNER | SYSTEM | WEBHOOK
//    val reason: String?,
//    val idempotencyKey: String?,
//    val webhookEventId: String?,
//    val payload: Map<String, Any?>?,   // masked subset
//)
//
//data class ProviderIds(
//    val pi: String? = null,
//    val ch: String? = null,
//    val pm: String? = null,
//    val cus: String? = null,
//)
