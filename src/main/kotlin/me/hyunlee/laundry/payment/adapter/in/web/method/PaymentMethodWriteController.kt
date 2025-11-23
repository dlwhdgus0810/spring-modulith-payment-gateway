package me.hyunlee.laundry.payment.adapter.`in`.web.method

import io.swagger.v3.oas.annotations.Operation
import me.hyunlee.laundry.common.adapter.`in`.web.ApiResponse
import me.hyunlee.laundry.common.domain.PaymentMethodId
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.payment.adapter.`in`.web.method.dto.*
import me.hyunlee.laundry.payment.application.port.`in`.method.PaymentMethodCommandUseCase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@Validated
@RestController
@RequestMapping("/api/payment-methods")
class PaymentMethodWriteController(
    private val command: PaymentMethodCommandUseCase
) {

    private val log : Logger = LoggerFactory.getLogger(PaymentMethodWriteController::class.java)

    @PostMapping
    @Operation(summary = "addPaymentMethod")
    fun create(auth: JwtAuthenticationToken, @RequestBody req : PaymentMethodRegisterRequest) : ResponseEntity<ApiResponse<Any>> {
        req.validateOrThrow()

        val currentUser = UUID.fromString(auth.token.claims["sub"] as String)

        val pm = when (PaymentMethodRegisterType.valueOf(req.type)) {
            PaymentMethodRegisterType.CARD -> command.create(req.toCardCommand(currentUser))
            PaymentMethodRegisterType.WALLET -> command.create(req.toWalletCommand(currentUser))
            PaymentMethodRegisterType.ACH -> command.create(req.toAchCommand(currentUser))
        }

        return ApiResponse.created(pm.toResponse())
    }

    @PostMapping("/setup-intents")
    @Operation(summary = "createSetupIntent")
    fun startSetupIntent(auth: JwtAuthenticationToken, @RequestBody req: StartSetupIntentRequest): ResponseEntity<ApiResponse<Any>> {
        val currentUser = UserId(UUID.fromString(auth.token.claims["sub"] as String))
        val result = command.startSetupIntent(currentUser, req.idempotentKey)
        val body = StartSetupIntentResponse(
            setupIntentId = result.setupIntentId,
            clientSecret = result.clientSecret,
            customerId = result.customerId
        )

        return ApiResponse.created(body)
    }

    @PostMapping("/setup-intents/finalize")
    @Operation(summary = "finalizeSetupIntent")
    fun finalizeSetupIntent(auth: JwtAuthenticationToken, @RequestBody req: FinalizeSetupIntentRequest): ResponseEntity<ApiResponse<Any>> {
        req.validateOrThrow()
        val currentUser = UserId(UUID.fromString(auth.token.claims["sub"] as String))
        val pm = command.finalizeSetupIntent(currentUser, req.setupIntentId, req.nickname, req.setAsDefault)
        return ApiResponse.created(pm)
    }

    @PostMapping("/{paymentMethodId}/default")
    @Operation(summary = "setDefaultPaymentMethod")
    fun setDefault(
        @PathVariable paymentMethodId: UUID
    , auth: JwtAuthenticationToken): ResponseEntity<ApiResponse<Any>> {
        val currentUser = UserId(UUID.fromString(auth.token.claims["sub"] as String))
        command.setDefault(currentUser, PaymentMethodId(paymentMethodId))
        return ApiResponse.success(mapOf("defaultSet" to true))
    }

    @DeleteMapping("/{paymentMethodId}/default")
    @Operation(summary = "unsetDefaultPaymentMethod")
    fun unsetDefault(
        @PathVariable paymentMethodId: UUID
    , auth: JwtAuthenticationToken): ResponseEntity<ApiResponse<Any>> {
        val currentUser = UserId(UUID.fromString(auth.token.claims["sub"] as String))
        command.unsetDefault(currentUser, PaymentMethodId(paymentMethodId))
        return ApiResponse.success(mapOf("defaultUnset" to true))
    }
}