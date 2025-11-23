package me.hyunlee.laundry.monitoring.adapter.out.slack

import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class SlackNotifier(
    private val slackProps : SlackConfigProps
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val rest = RestTemplate()

    fun notify(title: String, message: String, severity: String = "INFO", context: Map<String, String> = emptyMap()) {
        val text = buildString {
            append("[*").append(severity).append("*] ").append(title).append('\n')
            append(message)
            if (context.isNotEmpty()) {
                append("\ncontext: ")
                append(context.entries.joinToString { "${it.key}=${it.value}" })
            }
        }
        if (slackProps.webhookUrl.isBlank()) {
            log.info("[SlackNotifier:dry-run] {}", text)
            return
        }
        try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val payload = mapOf("text" to text)
            val entity = HttpEntity(payload, headers)
            rest.postForEntity(slackProps.webhookUrl, entity, String::class.java)
        } catch (e: Exception) {
            log.warn("[SlackNotifier] failed to send: {}", e.message)
        }
    }
}
