package me.hyunlee.laundry.common.application.idempotency

import com.fasterxml.jackson.databind.ObjectMapper
import me.hyunlee.laundry.common.adapter.out.persistence.IdempotencyKeyJpaRepository
import me.hyunlee.laundry.common.adapter.out.persistence.IdempotencyRecord
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class IdempotencyServiceImpl(
    private val repo: IdempotencyKeyJpaRepository,
    private val objectMapper: ObjectMapper
) : IdempotencyServicePort {

    @Transactional
    override fun <T : Any> execute(
        userId: UUID?,
        key: String,
        resourceType: String?,
        responseType: Class<T>,
        handler: () -> Pair<String?, T>
    ): T {
        repo.findByUserIdAndIdempotencyKey(userId, key)?.let { existing ->
            if (existing.responseJson != null) return objectMapper.readValue(existing.responseJson, responseType)
            else throw IllegalStateException("Already in progress. key=$key")
        }

        val record =
            try {
                repo.save(IdempotencyRecord(userId = userId, idempotencyKey = key, resourceType = resourceType))
            } catch (_: DataIntegrityViolationException) {
                val existing = repo.findByUserIdAndIdempotencyKey(userId, key) ?: throw IllegalStateException("Record exists but not found. key=$key")
                if (existing.responseJson != null) return objectMapper.readValue(existing.responseJson, responseType)
                else throw IllegalStateException("Already in progress. key=$key")
            }

        val (resourceId, result) = handler()

        record.resourceId = resourceId
        record.responseJson = objectMapper.writeValueAsString(result)

        repo.save(record)
        return result
    }
}
