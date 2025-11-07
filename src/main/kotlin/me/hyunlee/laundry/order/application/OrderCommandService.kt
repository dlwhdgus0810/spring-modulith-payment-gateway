package me.hyunlee.laundry.order.application

import me.hyunlee.laundry.order.domain.model.Order
import me.hyunlee.laundry.order.domain.model.vo.PaymentInfo
import me.hyunlee.laundry.order.domain.port.`in`.OrderCommandUseCase
import me.hyunlee.laundry.order.domain.port.`in`.command.CreateOrderCommand
import me.hyunlee.laundry.order.domain.port.out.OrderRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderCommandService(
    private val repo : OrderRepository,
    private val events: ApplicationEventPublisher
) : OrderCommandUseCase {

    @Transactional
    override fun create(command: CreateOrderCommand): Order {
        // userId, pmId validation needed
        // create snapshot

        val (order, event) = Order.create(command.userId, PaymentInfo(command.pmId), command.contact, command.schedule, command.bagCount, command.items, command.tip, command.idempotentKey)

        val saved = repo.save(order)

        events.publishEvent(event)

        return saved
    }
}