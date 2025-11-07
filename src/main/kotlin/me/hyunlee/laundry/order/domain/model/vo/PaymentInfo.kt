package me.hyunlee.laundry.order.domain.model.vo

import me.hyunlee.laundry.common.PaymentMethodId

data class PaymentInfo(
    val methodId: PaymentMethodId,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val snapshot: PaymentSnapshot? = null
)

enum class PaymentStatus {
    NOT_REQUIRED,   // (선결제 미사용 흐름)
    PENDING,        // 결제 시도/대기
    AUTHORIZED,     // 선승인 잡힘
    CAPTURED,       // 청구 완료
    FAILED,         // 실패
    REFUNDED        // 환불
}

data class PaymentSnapshot(
    val brand: String?,
    val last4: String?,
    val expMonth: Int?,
    val expYear: Int?,
    val nickname: String?
)