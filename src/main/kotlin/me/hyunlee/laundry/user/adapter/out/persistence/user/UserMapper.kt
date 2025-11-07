package me.hyunlee.laundry.user.adapter.out.persistence.user

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.user.domain.model.Address
import me.hyunlee.laundry.user.domain.model.User

fun UserEntity.toDomain() : User {
    return User(
        id = UserId(id),
        phone = phone,
        email = email,
        firstName = firstName,
        lastName = lastName,
        addresses = addresses.map { it.toDomain() },
        customerId = customerId
    )
}

fun User.toEntity() : UserEntity {
    val ue = UserEntity(
        id = id.value,
        phone = phone,
        email = email,
        firstName = firstName,
        lastName = lastName,
        customerId = customerId
    )

    ue.addresses = addresses.map { it.toEntity().apply { user = ue } }.toMutableList()

    return ue
}

fun AddressEntity.toDomain() : Address {
    return Address(
        id = id,
        street = street,
        city = city,
        state = state,
        postalCode = postalCode,
        secondary = secondary,
        instructions = instructions,
        isPrimary = isPrimary
    )
}

fun Address.toEntity() : AddressEntity {
    return AddressEntity(
        street = street,
        city = city,
        state = state,
        postalCode = postalCode,
        secondary = secondary,
        instructions = instructions,
        isPrimary = isPrimary,
    )
}