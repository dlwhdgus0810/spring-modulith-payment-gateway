package me.hyunlee.laundry.payment.adapter.`in`.web.method

import me.hyunlee.laundry.payment.adapter.`in`.web.method.dto.PaymentMethodResponse
import me.hyunlee.laundry.payment.adapter.`in`.web.method.dto.PaymentSummaryResponse
import me.hyunlee.laundry.payment.domain.model.method.AchInfo
import me.hyunlee.laundry.payment.domain.model.method.CardInfo
import me.hyunlee.laundry.payment.domain.model.method.PaymentMethod
import me.hyunlee.laundry.payment.domain.model.method.WalletInfo

fun PaymentMethod.toResponse(): PaymentMethodResponse = when (val s = info) {
    is CardInfo -> PaymentMethodResponse(
        id = id.toString(),
        isDefault = isDefault,
        summary = PaymentSummaryResponse(
            brand = s.summary.brand,
            last4 = s.summary.last4,
            expMonth = s.summary.expMonth,
            expYear = s.summary.expYear,
        ),
        wallet = null,
        bankName = null
    )
    is WalletInfo -> PaymentMethodResponse(
        id = id.toString(),
        isDefault = isDefault,
        summary = PaymentSummaryResponse(
            brand = s.summary?.brand,
            last4 = s.summary?.last4,
            expMonth = s.summary?.expMonth,
            expYear = s.summary?.expYear,
        ),
        wallet = s.wallet.toString(),
        bankName = null
    )
    is AchInfo -> PaymentMethodResponse(
        id = id.toString(),
        isDefault = isDefault,
        summary = PaymentSummaryResponse(
            brand = null,
            last4 = s.last4,
            expMonth = null,
            expYear = null,
        ),
        wallet = null,
        bankName = s.bankName
    )
}