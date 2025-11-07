package me.hyunlee.laundry.order.domain.model

import me.hyunlee.laundry.order.domain.model.catalog.AddOnType
import me.hyunlee.laundry.order.domain.model.catalog.ServiceAddonPolicy
import me.hyunlee.laundry.order.domain.model.catalog.ServiceType

data class OrderItem(
    val serviceType: ServiceType,
    val addOns: Set<AddOnType> = emptySet()
) {
    init { ServiceAddonPolicy.check(serviceType, addOns) }
}