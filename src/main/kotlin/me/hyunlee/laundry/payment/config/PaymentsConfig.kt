package me.hyunlee.laundry.payment.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(PaymentsProperties::class)
class PaymentsConfig
