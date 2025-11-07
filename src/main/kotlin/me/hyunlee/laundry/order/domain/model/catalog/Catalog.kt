package me.hyunlee.laundry.order.domain.model.catalog

enum class ServiceType {
    WASH_FOLD, DRY_CLEAN, IRONING, EXPRESS, BULK_LINEN
}

enum class AddOnType {
    HANG_DRY, FRAGRANCE, STAIN_REMOVAL, DELICATE_CARE, ECO_WASH
}

/** 서비스별 허용 애드온 매핑(정책 레이어에서 관리) */
object ServiceAddonPolicy {
    private val allowed: Map<ServiceType, Set<AddOnType>> = mapOf(
        ServiceType.WASH_FOLD to setOf(AddOnType.HANG_DRY, AddOnType.FRAGRANCE, AddOnType.ECO_WASH),
        ServiceType.DRY_CLEAN  to setOf(AddOnType.STAIN_REMOVAL, AddOnType.FRAGRANCE, AddOnType.DELICATE_CARE),
        ServiceType.IRONING    to setOf(AddOnType.DELICATE_CARE, AddOnType.FRAGRANCE),
        ServiceType.EXPRESS    to setOf(AddOnType.ECO_WASH),
        ServiceType.BULK_LINEN to setOf(AddOnType.STAIN_REMOVAL, AddOnType.ECO_WASH)
    )

    fun check(service: ServiceType, addOns: Set<AddOnType>) {
        val allow = allowed[service].orEmpty()
        val illegal = addOns.filterNot { it in allow }
        require(illegal.isEmpty()) { "AddOns $illegal not allowed for service=$service" }
    }
}