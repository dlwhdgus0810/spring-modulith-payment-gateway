package me.hyunlee.laundry.order.adapter.out.persistence.order

import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.order.adapter.out.persistence.orderitem.OrderItemEntity
import me.hyunlee.laundry.order.domain.model.Contact
import me.hyunlee.laundry.order.domain.model.Order
import me.hyunlee.laundry.order.domain.model.OrderId
import me.hyunlee.laundry.order.domain.model.OrderItem
import me.hyunlee.laundry.order.domain.model.vo.PaymentInfo
import me.hyunlee.laundry.order.domain.model.vo.PaymentSnapshot
import me.hyunlee.order.domain.model.vo.*

fun OrderEntity.toDomain(): Order {
    return Order(
        id = OrderId(id),
        userId = UserId(userId),
        payment = payment.toDomain(),
        contact = contact.toDomain(),
        schedule = schedule.toDomain(),
        bagCount = BagCount(bagCount),
        tip = tip?.toDomain(),
        status = status,
        idempotentKey = idempotentKey,
        items = items.map { it.toDomain() }
    )
}

fun Order.toEntity(): OrderEntity {
    val oe = OrderEntity(
        id = id.value,
        userId = userId.value,
        payment = payment.toEntity(),
        contact = contact.toEntity(),
        schedule = schedule.toEntity(),
        bagCount = bagCount.value, tip = tip?.toEntity(),
        status = status,
        idempotentKey = idempotentKey,
    )

    val childEntities = items.map { it.toEntity(oe) }
    oe.addItems(childEntities)

    return oe
}

fun OrderItem.toEntity(parent: OrderEntity): OrderItemEntity =
    OrderItemEntity(order = parent, serviceType = this.serviceType
    ).also { it.addOns = this.addOns.toMutableSet() }


fun OrderItemEntity.toDomain(): OrderItem =
    OrderItem(serviceType = serviceType, addOns = addOns.toMutableSet())

fun PaymentInfo.toEntity(): PaymentInfoEmbeddable {
    return PaymentInfoEmbeddable(
        id = methodId.value,
        status = status,
        snapshot = snapshot?.toEntity()
    )
}

fun PaymentInfoEmbeddable.toDomain(): PaymentInfo {
    return PaymentInfo(
        methodId = PaymentMethodId(id),
        status = status,
        snapshot = snapshot?.toDomain()
    )
}

fun PaymentSnapshot.toEntity(): PaymentSnapshotEmbeddable =
    PaymentSnapshotEmbeddable(brand = brand, last4 = last4, expMonth = expMonth, expYear = expYear, nickname = nickname)

fun PaymentSnapshotEmbeddable.toDomain(): PaymentSnapshot =
    PaymentSnapshot(brand = brand, last4 = last4, expMonth = expMonth, expYear = expYear, nickname = nickname)


fun ContactEmbeddable.toDomain(): Contact =
    Contact(phone = phone, email = email, address = address.toDomain())

fun Contact.toEntity(): ContactEmbeddable =
    ContactEmbeddable(phone = phone, email = email, address = address.toEntity())

fun Address.toEntity(): AddressEmbeddable =
    AddressEmbeddable(
        street = street,
        city = city,
        state = state,
        postalCode = postalCode,
        secondary = secondary,
        instructions = instructions
    )

fun AddressEmbeddable.toDomain(): Address =
    Address(
        street = street,
        city = city,
        state = state,
        postalCode = postalCode,
        secondary = secondary,
        instructions = instructions
    )

fun Schedule.toEntity(): ScheduleEmbeddable {
    return ScheduleEmbeddable(
        pickupDate = pickupDate,
        pickupSlot = pickupSlot.value,
        deliveryDate = deliveryDate,
        deliverySlot = deliverySlot?.value
    )
}

fun ScheduleEmbeddable.toDomain(): Schedule {
    return Schedule(
        pickupDate = pickupDate,
        pickupSlot = SlotIndex(pickupSlot),
        deliveryDate = deliveryDate,
        deliverySlot = deliverySlot?.let { SlotIndex(it) }
    )
}

fun TipEmbeddable.toDomain(): Tip =
    Tip(type = type, amount = amount)

fun Tip.toEntity(): TipEmbeddable =
    TipEmbeddable(amount = amount, type = type)







