//package me.hyunlee.laundry.common.adapter.`in`.scheduling
//
//import org.slf4j.LoggerFactory
//import org.springframework.modulith.events.core.EventPublicationRegistry
//import org.springframework.scheduling.annotation.Scheduled
//import org.springframework.stereotype.Component
//import java.util.concurrent.ConcurrentHashMap
//import java.util.concurrent.atomic.AtomicInteger
//
//@Component
//class PublicationReplayJob(
//    private val registry: EventPublicationRegistry,
//) {
//    private val log = LoggerFactory.getLogger(PublicationReplayJob::class.java)
//    private val MAX_ATTEMPTS = 2
//    private val map = ConcurrentHashMap<String, AtomicInteger>()
//
//    @Scheduled(fixedDelay = 5_000, initialDelay = 60_000)
//    fun replayIncomplete() {
//        registry.findIncompletePublications().forEach { pub ->
//            val key = pub.identifier.toString()
//            val count = map.computeIfAbsent(key) { AtomicInteger(0) }.incrementAndGet()
//            if (count <= MAX_ATTEMPTS) {
//                log.info("retrying invoked. target={}, attempt={}", pub.event.javaClass.simpleName, count)
//            } else {
//                registry.markCompleted(pub.event, pub.targetIdentifier)
//                log.info("retry max reached → marked completed. target={}", pub.event.javaClass.simpleName)
//            }
//        }
//    }
//}