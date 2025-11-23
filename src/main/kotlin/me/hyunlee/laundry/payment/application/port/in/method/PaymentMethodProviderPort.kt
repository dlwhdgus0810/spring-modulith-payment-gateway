package me.hyunlee.laundry.payment.application.port.`in`.method

import me.hyunlee.laundry.payment.domain.model.method.AchVerificationStatus
import me.hyunlee.laundry.payment.domain.model.method.PaymentSummary
import me.hyunlee.laundry.payment.domain.model.method.WalletType

/**
 * 결제수단(SetupIntent/PaymentMethod 스냅샷) 전용 프로바이더 포트
 */
interface PaymentMethodProviderPort {
    /**
     * Create a SetupIntent for client-side confirmation. Do NOT confirm server-side for new PMs.
     * Returns id, status, clientSecret, and nullable paymentMethodId (available after confirmation).
     */
    fun createPaymentMethodSetupIntent(
        customerId: String,
        usage: String = "off_session",
        idempotencyKey: String
    ): SetupIntentInfo

    /** Retrieve SetupIntent by id. */
    fun retrievePaymentMethodSetupIntent(setupIntentId: String): SetupIntentInfo

    /**
     * Confirm a SetupIntent to attach pm_* to cus_* and authorize off_session usage.
     * NOTE: Only safe for flows where PM already exists and SCA is not expected client-side.
     */
    fun confirmPaymentMethodSetupIntent(
        customerId: String,
        paymentMethodId: String,
        paymentMethodType: String,
        idempotencyKey: String?
    ): String

    /** Create and confirm an off-session PaymentIntent. Return its status. */
    fun createPaymentMethodIntentOffSession(
        customerId: String,
        paymentMethodId: String,
        amount: Long,
        currency: String,
        idempotencyKey: String?
    ): String

    /** Retrieve latest PM snapshot for refresh: returns map with brand/last4/expMonth/expYear/fingerprint/wallet if present. */
    fun retrievePaymentMethodInfo(paymentMethodId: String): RetrievedPmInfo
}

/** Lightweight projection of Stripe SetupIntent used at application layer. */
data class SetupIntentInfo(
    val id: String,
    val status: String?,
    val clientSecret: String?,
    val customerId: String?,
    val paymentMethodId: String?,
    val mandateId: String? = null
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