package me.hyunlee.laundry.order.application.port.`in`.command

import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.order.domain.model.OrderItem
import me.hyunlee.order.domain.model.vo.Address
import me.hyunlee.order.domain.model.vo.BagCount
import me.hyunlee.order.domain.model.vo.Schedule
import me.hyunlee.order.domain.model.vo.Tip

data class CreateOrderCommand(
    val userId: UserId,
    val pmId: PaymentMethodId,

    val address: Address,

    val schedule: Schedule,

    val bagCount: BagCount,
    val items: List<OrderItem>,
    val tip: Tip?,                       // 선택
)