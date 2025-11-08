package me.hyunlee.laundry.user.application

import me.hyunlee.laundry.user.application.port.`in`.AddAddressCommand
import me.hyunlee.laundry.user.application.port.`in`.RegisterUserCommand
import me.hyunlee.laundry.user.application.port.`in`.UpdateUserProfileCommand
import me.hyunlee.laundry.user.application.port.`in`.UserReadPort
import me.hyunlee.laundry.user.application.port.`in`.UserWritePort
import me.hyunlee.laundry.user.application.port.out.UserRepository
import me.hyunlee.laundry.user.domain.model.User
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserCommandService(
    private val repo : UserRepository,
    private val reader : UserReadPort,
    private val events: ApplicationEventPublisher
) : UserWritePort {

    @Transactional
    override fun register(command: RegisterUserCommand): User {
        reader.ensurePhoneAvailable(command.phone)

        val (user, event) = User.create(command.phone, command.email, command.firstName, command.lastName)

        val saved = repo.save(user)

//        events.publishEvent(event)

        return saved
    }

    @Transactional
    override fun updateProfile(command: UpdateUserProfileCommand): User {
        val user = reader.getById(command.userId)

        val (updated, event) = user.updateProfile(command.email, command.firstName, command.lastName)

        val saved = repo.save(updated)

        events.publishEvent(event)

        return saved
    }

    @Transactional
    override fun addAddress(command: AddAddressCommand): User {
        val user = reader.getById(command.userId)

        val updated = user.addAddress(command.address)

        return repo.save(updated)
    }
}