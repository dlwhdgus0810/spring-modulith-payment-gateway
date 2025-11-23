package me.hyunlee.laundry.monitoring.adapter.out.slack

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "slack")
data class SlackConfigProps(
    var webhookUrl: String
)

@Configuration
@EnableConfigurationProperties(SlackConfigProps::class)
class SlackConfig {}