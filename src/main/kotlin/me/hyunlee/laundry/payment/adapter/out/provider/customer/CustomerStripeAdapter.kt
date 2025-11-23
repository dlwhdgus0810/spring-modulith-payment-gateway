package me.hyunlee.laundry.payment.adapter.out.provider.customer

import com.stripe.Stripe
import com.stripe.model.Customer
import com.stripe.param.CustomerCreateParams
import com.stripe.param.CustomerSearchParams
import me.hyunlee.laundry.common.domain.UserId
import me.hyunlee.laundry.common.domain.event.payment.ProviderCustomerEnsuredEvent
import me.hyunlee.laundry.common.domain.event.payment.UserQueryPort
import me.hyunlee.laundry.payment.adapter.out.provider.stripeCall
import me.hyunlee.laundry.payment.application.port.`in`.customer.CustomerProviderPort
import me.hyunlee.laundry.payment.config.PaymentsProperties
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.util.*

@Component
class CustomerStripeAdapter(
    private val userQuery: UserQueryPort,
    private val publisher: ApplicationEventPublisher,
    private val paymentsProps: PaymentsProperties,
) : CustomerProviderPort {

    init { Stripe.apiKey = paymentsProps.stripe.privateKey }
    private val log = LoggerFactory.getLogger(CustomerStripeAdapter::class.java)
    private inline fun <T> stripe(block: () -> T): T = stripeCall(log, block = block)

    override fun ensureCustomerId(userId: String): String {
        val userIdObj = UserId(UUID.fromString(userId))

        userQuery.findCustomerId(userIdObj)?.let { existing -> return existing }

        val customerId = getOrCreateCustomerId(userId)
        publisher.publishEvent(ProviderCustomerEnsuredEvent(userIdObj, customerId))
        return customerId
    }

    private fun getOrCreateCustomerId(userId: String): String = stripe {
        if (paymentsProps.stripe.customer.searchByMetadata) {
            val query = "metadata['app_user_id']:'$userId'"
            val params = CustomerSearchParams.builder().setQuery(query).build()
            val result = Customer.search(params)
            val data = result.data

            if (data != null && data.isNotEmpty()) {
                log.info("customer retrieved from metadata")
                return data[0].id
            }
        }

        val create = CustomerCreateParams.builder()
            .putMetadata("app_user_id", userId)
            .build()

        val created = Customer.create(create)

        log.info("[PAYMENT-CUSTOMER] Stripe customer newly created app_user_id={}", userId)
        return created.id
    }

}
