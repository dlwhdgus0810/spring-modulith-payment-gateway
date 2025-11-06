package me.hyunlee.laundry.user.application

import me.hyunlee.laundry.user.domain.exception.DuplicatePhoneException
import me.hyunlee.laundry.user.domain.model.User
import me.hyunlee.laundry.user.domain.port.out.UserRepository
import me.hyunlee.user.domain.port.inbound.UserCommandUseCase
import me.hyunlee.user.domain.port.inbound.UserQueryUseCase
import me.hyunlee.user.domain.port.inbound.commands.AddAddressCommand
import me.hyunlee.user.domain.port.inbound.commands.RegisterUserCommand
import me.hyunlee.user.domain.port.inbound.commands.UpdateUserProfileCommand
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserCommandService(
    private val repo : UserRepository,
    private val query : UserQueryUseCase,
    private val events: ApplicationEventPublisher
) : UserCommandUseCase {

    @Transactional
    override fun register(command: RegisterUserCommand): User {
        require(repo.findByPhone(command.phone) == null) { throw DuplicatePhoneException("Phone already registered: ${command.phone}") }

        val (user, event) = User.create(command.phone, command.email, command.firstName, command.lastName)

        val saved = repo.save(user)

        events.publishEvent(event)

        return saved
    }

    @Transactional
    override fun updateProfile(command: UpdateUserProfileCommand): User {
        val user = query.getById(command.userId)

        val (updated, event) = user.updateProfile(command.email, command.firstName, command.lastName)

        val saved = repo.save(updated)

        events.publishEvent(event)

        return saved
    }

    @Transactional
    override fun addAddress(command: AddAddressCommand): User {
        val user = query.getById(command.userId)

        val updated = user.addAddress(command.address)

        return repo.save(updated)
    }
}