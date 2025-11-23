package me.hyunlee.laundry.payment.adapter.out.method

import com.stripe.model.PaymentIntent
import com.stripe.model.PaymentMethod
import com.stripe.model.SetupIntent
import com.stripe.net.RequestOptions
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.SetupIntentCreateParams
import me.hyunlee.laundry.payment.adapter.out.provider.StripeRequestOptionsFactory
import me.hyunlee.laundry.payment.adapter.out.provider.stripeCall
import me.hyunlee.laundry.payment.application.port.`in`.method.PaymentMethodProviderPort
import me.hyunlee.laundry.payment.application.port.`in`.method.RetrievedPmInfo
import me.hyunlee.laundry.payment.application.port.`in`.method.SetupIntentInfo
import me.hyunlee.laundry.payment.domain.model.method.AchVerificationStatus
import me.hyunlee.laundry.payment.domain.model.method.PaymentSummary
import me.hyunlee.laundry.payment.domain.model.method.WalletType.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class PaymentMethodStripeAdapter(
    private val requestOptionsFactory: StripeRequestOptionsFactory,
) : PaymentMethodProviderPort {

    private val log = LoggerFactory.getLogger(PaymentMethodStripeAdapter::class.java)
    private inline fun <T> stripe(block: () -> T): T = stripeCall(log, block = block)

    override fun createPaymentMethodSetupIntent(
        customerId: String,
        usage: String,
        idempotencyKey: String
    ): SetupIntentInfo = stripe {
        val usageEnum = when (usage.lowercase()) {
            "off_session" -> SetupIntentCreateParams.Usage.OFF_SESSION
            "on_session" -> SetupIntentCreateParams.Usage.ON_SESSION
            else -> SetupIntentCreateParams.Usage.OFF_SESSION
        }

        val params = SetupIntentCreateParams.builder()
            .setCustomer(customerId)
            .addPaymentMethodType("card")
            .addPaymentMethodType("link")
            .addPaymentMethodType("us_bank_account")
            .setUsage(usageEnum)
            .build()

        // type will be requires_action due to setConfirm true

        val options = requestOptionsFactory.create(idempotencyKey)
        val si = SetupIntent.create(params, options)

        log.info("[PAYMENT-METHOD] pm setup intent created, si: $si")

        SetupIntentInfo(
            id = si.id,
            status = si.status,
            clientSecret = si.clientSecret,
            customerId = si.customer,
            paymentMethodId = si.paymentMethod
        )
    }

    override fun retrievePaymentMethodSetupIntent(setupIntentId: String): SetupIntentInfo = stripe {
        val si = SetupIntent.retrieve(setupIntentId)

        log.info("[PAYMENT-METHOD] pm setup intent retrieved after confirm, si: $si.")

        SetupIntentInfo(
            id = si.id,
            status = si.status,
            clientSecret = si.clientSecret,
            customerId = si.customer,
            paymentMethodId = si.paymentMethod,
            mandateId = si.mandate
        )
    }

    override fun confirmPaymentMethodSetupIntent(
        customerId: String,
        paymentMethodId: String,
        paymentMethodType: String,
        idempotencyKey: String?
    ): String = stripe {
        val builder = SetupIntentCreateParams.builder()
            .setCustomer(customerId)
            .setPaymentMethod(paymentMethodId)
            .setConfirm(true)
            .setUsage(SetupIntentCreateParams.Usage.OFF_SESSION)
            .addPaymentMethodType(paymentMethodType)

        val params = builder.build()
        val options = requestOptionsFactory.create(idempotencyKey)

        val si = SetupIntent.create(params, options)
        log.info("[PAYMENT-METHOD] pm setup intent created and confirmed.. 서버에서 실행되지 않음. 클라에서 직접 confirm.")
        si.status
    }

    override fun createPaymentMethodIntentOffSession(
        customerId: String,
        paymentMethodId: String,
        amount: Long,
        currency: String,
        idempotencyKey: String?
    ): String = stripe {
        val params = PaymentIntentCreateParams.builder()
            .setCustomer(customerId)
            .setPaymentMethod(paymentMethodId)
            .setAmount(amount)
            .setCurrency(currency.lowercase(Locale.getDefault()))
            .setConfirm(true)
            .setOffSession(true)
            .build()

        val options = requestOptions(idempotencyKey)
        val pi = PaymentIntent.create(params, options)

        log.info("[PAYMENT-METHOD] pm off-session intent created and confirmed.")

        pi.status
    }

    private fun requestOptions(idempotencyKey: String?): RequestOptions {
        val builder = RequestOptions.builder()
        if (!idempotencyKey.isNullOrBlank()) builder.setIdempotencyKey(idempotencyKey)
        return builder.build()
    }

    override fun retrievePaymentMethodInfo(paymentMethodId: String): RetrievedPmInfo = stripe {
        val pm = PaymentMethod.retrieve(paymentMethodId)

        log.info("[PAYMENT-METHOD] pm created and retrieved, pm: $pm")

        when (pm.type) {
            "card" -> {
                val card = pm.card ?: error("Stripe PM.card is null for type=card, pm=$paymentMethodId")
                val walletType = card.wallet?.type // "apple_pay" | "google_pay" | "link" | null

                val summary = PaymentSummary(
                    brand = card.brand,
                    last4 = card.last4,
                    expMonth = card.expMonth?.toInt(),
                    expYear = card.expYear?.toInt(),
                )

                if (walletType.isNullOrBlank()) {
                    RetrievedPmInfo.Card(
                        summary = summary,
                        fingerprint = card.fingerprint
                    )
                } else {

                    val type = when (walletType.lowercase()) {
                        "apple_pay" -> APPLE_PAY
                        "google_pay" -> GOOGLE_PAY
                        "link" -> LINK
                        "naver_pay" -> NAVER_PAY
                        else -> throw IllegalArgumentException("Unsupported wallet type: $walletType")
                    }

                    RetrievedPmInfo.Wallet(
                        wallet = type,
                        summary = summary,   // 어떤 월렛은 brand/last4가 없을 수 있어 nullable 유지
                        fingerprint = card.fingerprint
                    )
                }
            }

            "us_bank_account" -> {
                val bank = pm.usBankAccount
                val bankName = bank?.bankName ?: error("bankName required for ACH, pm=$paymentMethodId")
                // Stripe SDK 버전에 따라 verification/mandate 노출 경로가 다름 → 후속 보강 예정

                // mandateId 시도: 메타데이터에 저장된 값이 있으면 사용 (없으면 null 유지)
                val mandateFromMetadata = try {
                    pm.metadata?.get("mandate_id")
                } catch (_: Exception) {
                    null
                }

                RetrievedPmInfo.Ach(
                    bankName = bankName,
                    last4 = bank.last4, // bank가 null 아닌 시점이므로 안전
                    verification = AchVerificationStatus.PENDING,
                    mandateId = mandateFromMetadata
                )
            }

            else -> error("Unsupported Stripe PM type: ${pm.type} (pm=$paymentMethodId)")
        }
    }

}