package me.hyunlee.laundry.payment.adapter.out.transaction

import com.stripe.model.Charge
import com.stripe.model.PaymentIntent
import com.stripe.model.Refund
import com.stripe.net.RequestOptions
import com.stripe.param.PaymentIntentCaptureParams
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.RefundCreateParams
import me.hyunlee.laundry.payment.adapter.out.provider.StripeRequestOptionsFactory
import me.hyunlee.laundry.payment.adapter.out.provider.stripeCall
import me.hyunlee.laundry.payment.application.port.`in`.transaction.*
import me.hyunlee.laundry.payment.domain.model.method.PaymentMethod
import me.hyunlee.laundry.payment.domain.model.method.PaymentMethodType.*
import me.hyunlee.laundry.payment.domain.model.method.WalletInfo
import me.hyunlee.laundry.payment.domain.model.method.WalletType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PaymentTransactionStripeAdapter(
    private val requestOptionsFactory: StripeRequestOptionsFactory,
) : PaymentTransactionProviderPort {

    private val log = LoggerFactory.getLogger(PaymentTransactionStripeAdapter::class.java)
    private inline fun <T> stripe(block: () -> T): T = stripeCall(log, block = block)

    override fun createPaymentIntentAuthorize(
        customerId: String,
        pm: PaymentMethod,
        amount: Long,
        onSession: Boolean,
        idempotencyKey: String?
    ): CreatedPiInfo = stripe {
        val builder = PaymentIntentCreateParams.builder()
            .setCustomer(customerId)
            .setAmount(amount)
            .setCurrency("usd")

        when (pm.info.methodType) {
            CARD -> {
                builder
                    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                    .addPaymentMethodType("card")
                    .putMetadata("type", "card")
            }
            ACH -> {
                builder
                    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.AUTOMATIC)
                    .addPaymentMethodType("us_bank_account")
                    .putMetadata("type", "us_bank_account")
            }
            WALLET -> {
                val walletInfo = pm.info as? WalletInfo
                    ?: throw IllegalStateException("WalletType must not be null for WALLET payment method.")

                when (walletInfo.wallet) {
                    WalletType.LINK -> {
                        builder
                            .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                            .addPaymentMethodType("link")
                            .putMetadata("type", "card")
                    }
                    else -> throw IllegalArgumentException("Naver Pay is not supported by Stripe.")
                }
            }
        }

        // on-session의 경우 confirm은 보통 클라이언트에서 수행하므로 여기서는 확정하지 않음
        // 필요 시 `.setConfirm(true)` 와 return_url 등을 설정하되, 현재 정책은 client-side confirm
        val params = builder.setPaymentMethod(pm.providerPmId).build()
        val options = requestOptions(idempotencyKey)
        val pi = PaymentIntent.create(params, options)
        val type = pi.metadata.get("type")


        log.info("[TRANSACTION] payment intent with manual capture method created.")

        CreatedPiInfo(
            id = pi.id,
            status = pi.status,
            type = type,
            clientSecret = pi.clientSecret,
            amount = pi.amount,
            currency = pi.currency
        )
    }

    override fun capturePaymentIntent(
        paymentIntentId: String,
        amount: Long?
    ): CaptureResult = stripe {

        val pi = if (amount == null) {
            val target = PaymentIntent.retrieve(paymentIntentId)
            target.capture()
        } else {
            val target = PaymentIntent.retrieve(paymentIntentId)
            val params = PaymentIntentCaptureParams.builder()
                .setAmountToCapture(amount)
                .build()
            target.capture(params)
        }

        val receiptUrl = pi.latestCharge?.let { cid ->
            runCatching { Charge.retrieve(cid).receiptUrl }.getOrElse { null }
        }

        log.info("[TRANSACTION] payment intent retrieved and captured.")

        CaptureResult(
            id = pi.id,
            status = pi.status,
            amountCaptured = pi.amountReceived,
            receiptUrl = receiptUrl
        )
    }

    override fun cancelPaymentIntent(paymentIntentId: String): String = stripe {
        val pi = PaymentIntent.retrieve(paymentIntentId)
        val canceled = pi.cancel()
        canceled.status
    }

    override fun retrievePaymentIntent(paymentIntentId: String): RetrievedPiInfo = stripe {
        val pi = PaymentIntent.retrieve(paymentIntentId)

        val receiptUrl = pi.latestCharge?.let { cid ->
            runCatching { Charge.retrieve(cid).receiptUrl }.getOrElse { null }
        }

        log.info("[TRANSACTION] payment intent retrieved.")

        RetrievedPiInfo(
            id = pi.id,
            status = pi.status,
            amount = pi.amount,
            currency = pi.currency,
            customerId = pi.customer,
            paymentMethodId = pi.paymentMethod,
            amountCapturable = pi.amountCapturable,
            latestChargeReceiptUrl = receiptUrl
        )

    }

    override fun refundPaymentIntent(
        paymentIntentId: String,
        amount: Long?,
        reason: String?,
        idempotencyKey: String?
    ): RefundResult = stripe {
        val pi = PaymentIntent.retrieve(paymentIntentId)
        val chargeId = pi.latestCharge ?: error("latest charge not found for pi=$paymentIntentId")

        val builder = RefundCreateParams.builder().setCharge(chargeId)

        if (amount != null) builder.setAmount(amount)
        if (!reason.isNullOrBlank()) builder.putMetadata("reason", reason)

        val params = builder.build()
        val options = requestOptionsFactory.create(idempotencyKey)

        val refund = Refund.create(params, options)
        RefundResult(
            refundId = refund.id,
            status = refund.status,
            amountRefunded = refund.amount,
            currency = refund.currency,
            chargeId = refund.charge,
            paymentIntentId = refund.paymentIntent
        )
    }

    override fun retrieveRefund(refundId: String): RefundInfo = stripe {
        val refund = Refund.retrieve(refundId)
        RefundInfo(
            refundId = refund.id,
            status = refund.status,
            amount = refund.amount,
            currency = refund.currency,
            chargeId = refund.charge,
            paymentIntentId = refund.paymentIntent
        )
    }

    private fun requestOptions(idempotencyKey: String?): RequestOptions {
        val builder = RequestOptions.builder()
        if (!idempotencyKey.isNullOrBlank()) builder.setIdempotencyKey(idempotencyKey)
        return builder.build()
    }

}