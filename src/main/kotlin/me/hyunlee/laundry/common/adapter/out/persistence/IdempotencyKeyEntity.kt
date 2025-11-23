package me.hyunlee.laundry.common.adapter.out.persistence

import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "idempotency_records",
    uniqueConstraints = [
        UniqueConstraint(
            name = "ux_idempotency_user_key",
            columnNames = ["user_id", "idempotency_key"]
        )
    ]
)
class IdempotencyRecord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = true)
    var userId: UUID? = null,

    @Column(name = "idempotency_key", nullable = false, length = 200)
    var idempotencyKey: String,

    // "ORDER", "PAYMENT" 등 도메인 구분용 (선택)
    @Column(name = "resource_type", length = 50, nullable = true)
    var resourceType: String? = null,

    // 실제 비즈니스 리소스 ID (orderId, paymentTxId 등)
    @Column(name = "resource_id", length = 100, nullable = true)
    var resourceId: String? = null,

    @Lob
    @Column(name = "response_json", columnDefinition = "LONGTEXT", nullable = true)
    var responseJson: String? = null
) : BaseEntity()

enum class IdempotencyStatus { PENDING, DONE, FAILED }
