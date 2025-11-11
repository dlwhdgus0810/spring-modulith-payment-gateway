package me.hyunlee.laundry.user.application

import me.hyunlee.laundry.common.domain.phone.PhoneNumberNormalizer
import me.hyunlee.laundry.user.application.port.`in`.*
import me.hyunlee.laundry.user.application.port.out.UserRepository
import me.hyunlee.laundry.user.domain.model.User
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserCommandService(
    private val repo : UserRepository,
    private val reader : UserReadPort,
    private val events: ApplicationEventPublisher,
    private val phoneNorm: PhoneNumberNormalizer,
) : UserWritePort {

    @Transactional
    override fun register(command: RegisterUserCommand): User {
        val normalized = try { phoneNorm.normalizeToE164(command.phone, null) } catch (e: IllegalArgumentException) { throw IllegalArgumentException("Invalid phone number") }

        reader.ensurePhoneAvailable(normalized)

        val (user, event) = User.create(normalized, command.email, command.firstName, command.lastName)

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