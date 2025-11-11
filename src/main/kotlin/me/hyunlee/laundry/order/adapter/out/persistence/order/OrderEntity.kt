package me.hyunlee.laundry.order.adapter.out.persistence.order

import jakarta.persistence.*
import me.hyunlee.laundry.common.adapter.out.persistence.BaseEntity
import me.hyunlee.laundry.order.adapter.out.persistence.orderitem.OrderItemEntity
import me.hyunlee.order.domain.model.enums.OrderStatus
import java.util.*

@Entity
@Table(name = "orders",)
class OrderEntity(
    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    var id: UUID,

    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    var userId: UUID,

    @Embedded
    var payment: PaymentInfoEmbeddable,

    @Embedded
    var contact: ContactEmbeddable,

    @Embedded
    var schedule: ScheduleEmbeddable,

    @Column(name = "bag_count", nullable = false)
    var bagCount: Int,

    @Embedded
    var tip: TipEmbeddable? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", length = 32, nullable = false)
    var status: OrderStatus,

    var idempotentKey: String? = null,

) : BaseEntity() {

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    private val _items: MutableList<OrderItemEntity> = mutableListOf()

    val items: List<OrderItemEntity> get() = _items  // 불변뷰 공개

    fun addItems(items: Iterable<OrderItemEntity>) =
        items.forEach { addItem(it) }

    fun addItem(item: OrderItemEntity) {
        item.order = this
        _items.add(item)
    }

}