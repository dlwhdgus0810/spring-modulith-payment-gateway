package me.hyunlee.laundry.user.domain.exception

class DuplicateUserException(message: String) : RuntimeException(message)
class DuplicatePhoneException(message: String) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)