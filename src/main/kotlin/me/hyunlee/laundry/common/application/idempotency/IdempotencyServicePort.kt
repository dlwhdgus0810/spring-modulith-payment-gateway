package me.hyunlee.laundry.common.application.idempotency

import java.util.*

interface IdempotencyServicePort {
    fun <T : Any> execute(
        userId: UUID?,
        key: String,
        resourceType: String?,              // "ORDER", "PAYMENT" 등
        responseType: Class<T>,
        handler: () -> Pair<String?, T>     // Pair(resourceId, response)
    ): T
}