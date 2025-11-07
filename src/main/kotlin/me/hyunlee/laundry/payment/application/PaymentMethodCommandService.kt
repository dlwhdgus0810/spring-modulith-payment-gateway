package me.hyunlee.laundry.payment.application

import me.hyunlee.laundry.payment.domain.model.PaymentMethod
import me.hyunlee.laundry.payment.domain.port.`in`.CreateCardOrWalletCommand
import me.hyunlee.laundry.payment.domain.port.`in`.PaymentMethodCommandUseCase
import me.hyunlee.laundry.payment.domain.port.out.PaymentMethodRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentMethodCommandService(
    private val repo: PaymentMethodRepository
) : PaymentMethodCommandUseCase {

    @Transactional
    override fun create(cmd: CreateCardOrWalletCommand): PaymentMethod {
        val pm = PaymentMethod.create(cmd.toSpec())

        val saved = repo.save(pm)

        return saved
    }

}