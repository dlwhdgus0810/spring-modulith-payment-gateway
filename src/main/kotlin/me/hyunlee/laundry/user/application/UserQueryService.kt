package me.hyunlee.laundry.user.application

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.common.domain.phone.PhoneNumberNormalizer
import me.hyunlee.laundry.user.application.port.`in`.UserReadPort
import me.hyunlee.laundry.user.application.port.out.UserRepository
import me.hyunlee.laundry.user.domain.exception.UserException.DuplicatePhoneException
import me.hyunlee.laundry.user.domain.exception.UserException.UserNotFoundException
import me.hyunlee.laundry.user.domain.model.User
import org.springframework.stereotype.Service
@Service
class UserQueryService(
    private val repo : UserRepository,
    private val phoneNorm: PhoneNumberNormalizer,
) : UserReadPort {

    override fun getAll(): List<User> =
        repo.findAll()

    override fun getById(id: UserId): User =
        repo.findById(id) ?: throw UserNotFoundException("User not found: ${id.value}")

    override fun getByCustomerId(customerId: String): User =
        repo.findByCustomerId(customerId) ?: throw UserNotFoundException("User not found: $customerId")

    override fun getByPhone(phone: String): User {
        val normalized = try { phoneNorm.normalizeToE164(phone, null) } catch (e: IllegalArgumentException) { phone }
        return repo.findByPhone(normalized) ?: throw UserNotFoundException("User not found, phone: $normalized")
    }

    override fun ensurePhoneAvailable(phone: String) {
        val normalized = try { phoneNorm.normalizeToE164(phone, null) } catch (e: IllegalArgumentException) { throw DuplicatePhoneException(phone) }
        repo.findByPhone(normalized)?.let { throw DuplicatePhoneException(normalized) }
    }

}