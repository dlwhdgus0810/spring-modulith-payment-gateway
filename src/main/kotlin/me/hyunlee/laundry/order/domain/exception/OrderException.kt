package me.hyunlee.laundry.order.domain.exception

sealed class OrderException(message: String) : RuntimeException(message) {
    class UserNotFoundException(userId: String) : OrderException("User not found: $userId")
}

