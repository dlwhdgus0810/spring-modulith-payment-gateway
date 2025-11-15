package me.hyunlee.laundry.payment.application.port.`in`.method

import me.hyunlee.laundry.payment.domain.model.method.AchVerificationStatus
import me.hyunlee.laundry.payment.domain.model.method.PaymentSummary
import me.hyunlee.laundry.payment.domain.model.method.WalletType

interface ProviderPort {
    /** Ensure customer exists for the given domain user id string, and return cus_* id. */
    fun ensureCustomer(userId: String): String

    /**
     * Create a SetupIntent for client-side confirmation. Do NOT confirm server-side for new PMs.
     * Returns id, status, clientSecret, and nullable paymentMethodId (available after confirmation).
     */
    fun createSetupIntent(
        customerId: String,
        paymentMethodType: String = "card",
        usage: String = "off_session",
        idempotencyKey: String? = null
    ): SetupIntentInfo

    /** Retrieve SetupIntent by id. */
    fun retrieveSetupIntent(setupIntentId: String): SetupIntentInfo

    /**
     * Confirm a SetupIntent to attach pm_* to cus_* and authorize off_session usage.
     * NOTE: Only safe for flows where PM already exists and SCA is not expected client-side.
     */
    fun confirmSetupIntent(
        customerId: String,
        paymentMethodId: String,
        paymentMethodType: String,
        idempotencyKey: String?
    ): String

    /** Create and confirm an off-session PaymentIntent. Return its status. */
    fun createPaymentIntentOffSession(
        customerId: String,
        paymentMethodId: String,
        amount: Long,
        currency: String,
        idempotencyKey: String?
    ): String

    /** Retrieve latest PM snapshot for refresh: returns map with brand/last4/expMonth/expYear/fingerprint/wallet if present. */
    fun retrievePaymentInfo(paymentMethodId: String): RetrievedPmInfo

    /** Create a PaymentIntent with manual capture for authorization (pre-authorization). */
    fun createPaymentIntentAuthorize(
        customerId: String,
        paymentMethodId: String,
        amount: Long,
        currency: String,
        onSession: Boolean,
        idempotencyKey: String? = null
    ): CreatedPiInfo

    /** Capture a previously authorized PaymentIntent. Optionally support partial capture by amount. */
    fun capturePaymentIntent(paymentIntentId: String, amount: Long? = null): CaptureResult

    /** Cancel an authorized (uncaptured) PaymentIntent. */
    fun cancelPaymentIntent(paymentIntentId: String): String

    /** Retrieve PaymentIntent snapshot (status, amounts, etc.) for finalize-authorize or diagnostics. */
    fun retrievePaymentIntent(paymentIntentId: String): RetrievedPiInfo

    /**
     * Create a refund for a captured charge derived from the given PaymentIntent.
     * - If amount is null, refunds the full captured amount.
     * - reason is optional and provider-specific.
     * - Uses idempotencyKey to prevent duplicate refunds.
     */
    fun refundPaymentIntent(
        paymentIntentId: String,
        amount: Long? = null,
        reason: String? = null,
        idempotencyKey: String? = null
    ): RefundResult

    /** Retrieve a refund by id (provider re_*). */
    fun retrieveRefund(refundId: String): RefundInfo
}

/** Lightweight projection of Stripe SetupIntent used at application layer. */
data class SetupIntentInfo(
    val id: String,
    val status: String?,
    val clientSecret: String?,
    val customerId: String?,
    val paymentMethodId: String?
)

/** Lightweight projection for created PaymentIntent (authorization stage). */
data class CreatedPiInfo(
    val id: String,
    val status: String?,
    val clientSecret: String?,
    val amount: Long?,
    val currency: String?
)

/** Lightweight projection for captured result. */
data class CaptureResult(
    val id: String,
    val status: String?,
    val amountCaptured: Long?,
    val receiptUrl: String?
)

/** Lightweight projection of PaymentIntent used for finalize-authorize and monitoring. */
data class RetrievedPiInfo(
    val id: String,
    val status: String?,
    val amount: Long?,
    val currency: String?,
    val customerId: String?,
    val paymentMethodId: String?,
    val amountCapturable: Long?,
    val latestChargeReceiptUrl: String?
)

/** Lightweight projection for refund creation result. */
data class RefundResult(
    val refundId: String,
    val status: String?,
    val amountRefunded: Long?,
    val currency: String?,
    val chargeId: String?,
    val paymentIntentId: String?
)

/** Lightweight projection for refund snapshot. */
data class RefundInfo(
    val refundId: String,
    val status: String?,
    val amount: Long?,
    val currency: String?,
    val chargeId: String?,
    val paymentIntentId: String?
)

sealed interface RetrievedPmInfo {

    data class Card(
        val summary: PaymentSummary,
        val fingerprint: String?
    ) : RetrievedPmInfo

    data class Wallet(
        val wallet: WalletType,
        val summary: PaymentSummary?,
        val fingerprint: String?
    ) : RetrievedPmInfo

    data class Ach(
        val bankName: String,
        val last4: String?,
        val verification: AchVerificationStatus = AchVerificationStatus.PENDING,
        val mandateId: String? = null // 필요 시 Stripe에서 조회 가능하면 채우기
    ) : RetrievedPmInfo
}