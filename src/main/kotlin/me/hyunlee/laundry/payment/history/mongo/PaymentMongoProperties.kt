//package me.hyunlee.laundry.payment.history.mongo
//
//import org.springframework.boot.context.properties.ConfigurationProperties
//import org.springframework.boot.context.properties.bind.DefaultValue
//
//@ConfigurationProperties(prefix = "payments.mongo")
//data class PaymentMongoProperties(
//    @DefaultValue("false")
//    val enabled: Boolean = false,
//    @DefaultValue("false")
//    val projectorEnabled: Boolean = false,
//    @DefaultValue("false")
//    val snapshotsEnabled: Boolean = false,
//    @DefaultValue("payment_transaction_events")
//    val eventCollection: String = "payment_transaction_events",
//    @DefaultValue("payment_snapshots")
//    val snapshotCollection: String = "payment_snapshots",
//)
