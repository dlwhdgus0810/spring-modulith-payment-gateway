package me.hyunlee.laundry.order.adapter.out.persistence.orderitem

import jakarta.persistence.*
import me.hyunlee.laundry.order.adapter.out.persistence.order.AddOnsJsonConverter
import me.hyunlee.laundry.order.adapter.out.persistence.order.OrderEntity
import me.hyunlee.laundry.order.domain.model.catalog.AddOnType
import me.hyunlee.laundry.order.domain.model.catalog.ServiceType

@Entity
@Table(name = "order_items")
class OrderItemEntity(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, columnDefinition = "BINARY(16)")
    var order: OrderEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", length = 32, nullable = false)
    var serviceType: ServiceType,

    @Column(name = "addons", columnDefinition = "json")
    @Convert(converter = AddOnsJsonConverter::class)
    var addOns: Set<AddOnType> = emptySet()

)