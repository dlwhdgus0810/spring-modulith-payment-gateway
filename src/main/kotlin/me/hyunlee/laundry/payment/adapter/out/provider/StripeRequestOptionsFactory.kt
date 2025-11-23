package me.hyunlee.laundry.payment.adapter.out.provider

import com.stripe.net.RequestOptions
import org.springframework.stereotype.Component

@Component
class StripeRequestOptionsFactory {
    fun create(idempotencyKey: String? = null): RequestOptions {
        val builder = RequestOptions.builder()
        if (!idempotencyKey.isNullOrBlank()) {
            builder.setIdempotencyKey(idempotencyKey)
        }
        return builder.build()
    }
}
