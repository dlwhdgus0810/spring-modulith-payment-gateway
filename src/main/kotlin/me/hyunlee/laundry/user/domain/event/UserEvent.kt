package me.hyunlee.laundry.user.domain.event

import java.time.Instant
import java.util.*

interface UserEvent {
    val occurredAt: Instant
    val aggregateType: String
    val aggregateId: String
    val eventType: String
    val eventId: String
}

data class UserRegisteredEvent(
    val userId: String,
    val phone: String,
    val email: String?,
    val firstName: String,
    val lastName: String,
    override val occurredAt: Instant = Instant.now(),
    override val eventId: String = UUID.randomUUID().toString(),
) : UserEvent {
    override val aggregateType = "user"
    override val aggregateId = userId
    override val eventType = "UserRegistered"
}

data class UserUpdatedEvent(
    val userId: String,
    val email: String?,
    val firstName: String,
    val lastName: String,
    override val occurredAt: Instant = Instant.now(),
    override val eventId: String = UUID.randomUUID().toString()
) : UserEvent {
    override val aggregateType = "user"
    override val aggregateId = userId
    override val eventType = "UserUpdated"
}