package me.hyunlee.laundry.user.adapter.out.userdirectory

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.common.domain.port.UserDirectoryPort
import me.hyunlee.laundry.user.application.port.`in`.RegisterUserCommand
import me.hyunlee.laundry.user.application.port.`in`.UserReadPort
import me.hyunlee.laundry.user.application.port.`in`.UserWritePort
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserDirectoryPortAdapter(
    private val read: UserReadPort,
    private val write: UserWritePort,
) : UserDirectoryPort {

    override fun findById(userId: UUID): UserDirectoryPort.UserSummary? = try {
        val u = read.getById(UserId(userId))
        UserDirectoryPort.UserSummary(u.id.value, u.phone, u.email, u.firstName, u.role)
    } catch (_: Exception) { null }

    override fun findByPhone(phone: String): UserDirectoryPort.UserSummary? = try {
        val u = read.getByPhone(phone)
        UserDirectoryPort.UserSummary(u.id.value, u.phone, u.email, u.firstName, u.role)
    } catch (_: Exception) { null }

    override fun createByPhone(phone: String, firstName: String, lastName: String, email: String?): UserDirectoryPort.UserSummary {
        return try {
            val saved = write.register(
                RegisterUserCommand(
                    phone = phone,
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                )
            )
            UserDirectoryPort.UserSummary(saved.id.value, saved.phone, saved.email, saved.firstName, saved.role)
        } catch (_: Exception) {
            val u = read.getByPhone(phone)
            UserDirectoryPort.UserSummary(u.id.value, u.phone, u.email, u.firstName, u.role)
        }
    }

    override fun maskedEmailOf(user: UserDirectoryPort.UserSummary): String? {
        val email = user.email ?: return null
        val parts = email.split("@")
        if (parts.size != 2) return null
        val name = parts[0]
        val domain = parts[1]
        val maskedName = if (name.length <= 1) "*" else name.first() + "*".repeat(name.length - 1)
        val domainParts = domain.split(".")
        val maskedDomain = if (domainParts.isEmpty()) domain else run {
            val head = domainParts[0]
            val maskedHead = if (head.length <= 1) "*" else head.first() + "*".repeat(head.length - 1)
            val tail = if (domainParts.size > 1) domainParts.subList(1, domainParts.size) else emptyList()
            val sb = StringBuilder()
            sb.append(maskedHead)
            for (t in tail) { sb.append('.').append(t) }
            sb.toString()
        }
        return "$maskedName@$maskedDomain"
    }
}
