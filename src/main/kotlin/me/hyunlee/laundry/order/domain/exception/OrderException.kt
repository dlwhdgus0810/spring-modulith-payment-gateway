package me.hyunlee.laundry.order.domain.exception

class UserNotFoundException(userId: String) : RuntimeException("User not found: $userId")