package me.hyunlee.laundry.payment.domain.model.transaction

import me.hyunlee.laundry.common.domain.UserId
import java.time.OffsetDateTime
import java.util.*

data class PaymentAuthorization(
    val id: UUID? = null,
    val orderId: UUID,
    val userId: UserId,
    val paymentIntentId: String,
    val amountAuthorized: Long,
    val currency: String,
    val status: AuthorizationStatus,
    val expiresAt: OffsetDateTime?,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)

enum class AuthorizationStatus { REQUIRES_CONFIRMATION, REQUIRES_CAPTURE, CAPTURED, CANCELED, EXPIRED }

data class PaymentCaptureRecord(
    val id: UUID? = null,
    val orderId: UUID,
    val paymentIntentId: String,
    val amountCaptured: Long,
    val receiptUrl: String?,
    val status: CaptureStatus,
    val capturedAt: OffsetDateTime? = null,
)

enum class CaptureStatus { SUCCEEDED, FAILED }
