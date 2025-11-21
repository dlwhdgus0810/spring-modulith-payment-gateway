package me.hyunlee.laundry.order.application

import me.hyunlee.laundry.common.application.idempotency.IdempotencyServicePort
import me.hyunlee.laundry.common.domain.port.UserDirectoryPort
import me.hyunlee.laundry.order.application.port.`in`.OrderCommandUseCase
import me.hyunlee.laundry.order.application.port.`in`.command.CreateOrderCommand
import me.hyunlee.laundry.order.application.port.out.OrderRepository
import me.hyunlee.laundry.order.domain.exception.OrderException
import me.hyunlee.laundry.order.domain.model.Contact
import me.hyunlee.laundry.order.domain.model.Order
import me.hyunlee.laundry.order.domain.model.vo.PaymentInfo
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderCommandService(
    private val repo: OrderRepository,
    private val userPort : UserDirectoryPort,
    private val idemPort: IdempotencyServicePort,
    private val events: ApplicationEventPublisher
) : OrderCommandUseCase {

    private val log = LoggerFactory.getLogger(OrderCommandService::class.java)

    @Transactional
    override fun create(command: CreateOrderCommand): Order {
        val idemKey = "order_create:${command.userId}:${command.schedule.pickupDate}:${command.schedule.pickupSlot}:${command.pmId}"
        val userId = command.userId.value

        return idemPort.execute(
            userId = userId,
            key = idemKey,
            resourceType = "ORDER_CREATE",
            responseType = Order::class.java
        ) {
            val user = userPort.findById(userId) ?: throw OrderException.UserNotFoundException(userId.toString())

            val contact = Contact(phone = user.phone, email = user.email, address = command.address)

            val (order, event) = Order.create(command.userId, PaymentInfo(command.pmId), contact, command.schedule, command.bagCount, command.items, command.tip)

            log.info("create order: $order")

            val saved = repo.save(order)

            // events.publishEvent(event)

            saved.id.toString() to saved
        }
    }
}