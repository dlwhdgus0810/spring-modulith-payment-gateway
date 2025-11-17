package me.hyunlee.laundry.order.domain.exception

sealed class OrderException(message: String) : RuntimeException(message) {
    class UserNotFoundException(userId: String) : OrderException("User not found: $userId")
    class OrderNotFoundException(orderId: String) : OrderException("Order not found, orderId: $orderId")
}

