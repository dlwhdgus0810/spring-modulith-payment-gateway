package me.hyunlee.laundry.user.adapter.`in`.web

import me.hyunlee.laundry.user.application.port.`in`.UserWritePort
import me.hyunlee.user.adapter.`in`.web.dto.AddAddressRequest
import me.hyunlee.user.adapter.`in`.web.dto.RegisterUserRequest
import me.hyunlee.user.adapter.`in`.web.dto.UpdateUserProfileRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*

@Validated
@RestController
@RequestMapping("/api/users")
class UserWriteController(
    private val command : UserWritePort
) {

    @PostMapping
    suspend fun register(@RequestBody req: RegisterUserRequest): ResponseEntity<Any> {
        val saved = command.register(req.toCommand())
        val location = URI.create("/api/users/${saved.id.value}")
        return ResponseEntity.created(location).body(saved)
    }

    @PutMapping("/{id}")
    suspend fun updateProfile(
        @PathVariable id: UUID,
        @RequestBody req: UpdateUserProfileRequest
    ): ResponseEntity<Any> {
        val updated = command.updateProfile(req.toCommand(id))
        return ResponseEntity.ok(updated)
    }

    @PostMapping("/{id}/addresses")
    suspend fun addAddress(
        @PathVariable id: UUID,
        @RequestBody req: AddAddressRequest
    ): ResponseEntity<Any> {
        val saved = command.addAddress(req.toCommand(id))
        return ResponseEntity.status(HttpStatus.OK).body(saved)
    }
}