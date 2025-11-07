package me.hyunlee.laundry.user.domain.exception

sealed class UserException(message: String) : RuntimeException(message) {

    class DuplicateUserException(message: String) : UserException(message)
    class DuplicatePhoneException(phone: String) : UserException("Phone already registered: $phone")
    class UserNotFoundException(message: String) : UserException(message)

}