package me.hyunlee.laundry.auth.adapter.out.external.aws

import me.hyunlee.laundry.auth.application.port.out.SmsSenderPort
import me.hyunlee.laundry.auth.domain.exception.AuthException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest

@Component
class SnsSmsSenderAdapter(
    private val sns: SnsClient,
    @Value("\${auth.sms.sns.senderId:LAUNDRY}") private val senderId: String,
    @Value("\${auth.sms.sns.smsType:Transactional}") private val smsType: String,
    @Value("\${auth.sms.sns.maxPrice:0.50}") private val maxPrice: String,
) : SmsSenderPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendOtp(toPhoneE164: String, code: String, expiresInSec: Long) {
        val minutes = (expiresInSec / 60).coerceAtLeast(1)
        val message = "인증코드 $code. ${minutes}분 내에 입력하세요."

        val attributes = HashMap<String, MessageAttributeValue>()
        attributes["AWS.SNS.SMS.SenderID"] = attr(senderId)
        attributes["AWS.SNS.SMS.SMSType"] = attr(smsType) // Promotional | Transactional
        attributes["AWS.SNS.SMS.MaxPrice"] = attr(maxPrice)

        val req = PublishRequest.builder()
            .message(message)
            .phoneNumber(toPhoneE164)
            .messageAttributes(attributes)
            .build()

        try {
            val res = sns.publish(req)
            log.info("[SNS SMS] sent to={} messageId={}", toPhoneE164, res.messageId())
        } catch (ex: Exception) {
            log.error("[SNS SMS] failed to={} reason={}", toPhoneE164, ex.message, ex)
            throw AuthException.RateLimited("Failed to send SMS")
        }
    }

    private fun attr(value: String): MessageAttributeValue =
        MessageAttributeValue.builder().dataType("String").stringValue(value).build()
}
