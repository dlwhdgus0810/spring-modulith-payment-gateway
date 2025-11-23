package me.hyunlee.laundry.auth.adapter.out.external.aws

import me.hyunlee.laundry.auth.application.port.out.EmailSenderPort
import me.hyunlee.laundry.auth.domain.exception.AuthException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.*

@Component
class SesEmailSenderAdapter(
    private val ses: SesClient,
    @Value("\${auth.email.ses.from}") private val from: String,
    @Value("\${auth.email.ses.subject:Your verification code}") private val subject: String,
) : EmailSenderPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendOtp(toEmail: String, code: String, expiresInSec: Long) {
        val minutes = (expiresInSec / 60).coerceAtLeast(1)
        val bodyText = "Your verification code is $code. It expires in ${minutes} minutes."

        val req = SendEmailRequest.builder()
            .destination(Destination.builder().toAddresses(toEmail).build())
            .source(from)
            .message(
                Message.builder()
                    .subject(Content.builder().data(subject).charset("UTF-8").build())
                    .body(
                        Body.builder()
                            .text(Content.builder().data(bodyText).charset("UTF-8").build())
                            .build()
                    )
                    .build()
            )
            .build()
        try {
            val res = ses.sendEmail(req)
            log.info("[SES Email] sent to={} messageId={}", toEmail, res.messageId())
        } catch (ex: Exception) {
            log.error("[SES Email] failed to={} reason={}", toEmail, ex.message, ex)
            throw AuthException.RateLimited("Failed to send email")
        }
    }
}
