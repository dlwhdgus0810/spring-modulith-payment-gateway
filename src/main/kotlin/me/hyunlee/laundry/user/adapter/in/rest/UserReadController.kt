package me.hyunlee.laundry.user.adapter.`in`.rest

import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.user.domain.port.inbound.UserReadPort
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@Validated
@RestController
@RequestMapping("/api/users")
class UserReadController(
    private val query: UserReadPort
) {

    @GetMapping("/{id}")
    suspend fun get(@PathVariable id: UUID): ResponseEntity<Any> {
        val user = query.getById(UserId(id))
        return ResponseEntity.ok(user)
    }

    @GetMapping(params = ["phone"])
    suspend fun getByPhone(@RequestParam phone: String): ResponseEntity<Any> {
        val user = query.getByPhone(phone)
        return ResponseEntity.ok(user)
    }
}