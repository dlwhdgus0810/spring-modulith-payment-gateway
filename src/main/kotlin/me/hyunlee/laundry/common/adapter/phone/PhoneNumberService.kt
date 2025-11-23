package me.hyunlee.laundry.common.adapter.phone

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import me.hyunlee.laundry.common.domain.phone.PhoneNumberNormalizer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Centralized phone number normalization/validation using libphonenumber.
 * - Normalization policy: store and operate with E.164 (e.g., +14155552671)
 * - Default region is configurable (e.g., US, KR)
 */
@Component
class PhoneNumberService(
    @Value("\${phone.defaultRegion:US}") private val defaultRegion: String,
    @Value("\${phone.allowNationalInput:true}") private val allowNationalInput: Boolean,
) : PhoneNumberNormalizer {

    private val log = LoggerFactory.getLogger(javaClass)
    private val util: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    /**
     * Normalize any acceptable phone input to E.164 string. Throws IllegalArgumentException if invalid.
     */
    override fun normalizeToE164(raw: String, region: String?): String {
        val trimmed = raw.trim().replace("\u00A0", " ").replace(" ", "")

        if (trimmed.isEmpty()) throw IllegalArgumentException("Phone number cannot be blank")

        val parseRegion = when {
            trimmed.startsWith("+") -> null // region not needed for E.164
            else -> (region ?: defaultRegion).takeIf { allowNationalInput }
        }

        val proto = try {
            util.parse(trimmed, parseRegion)
        } catch (e: NumberParseException) {
            throw IllegalArgumentException("Invalid phone number format")
        }
        if (!util.isValidNumber(proto)) {
            throw IllegalArgumentException("Invalid phone number")
        }

        // Optional policy: reject toll-free/shared-cost/etc. Keep simple: allow all valid types for now
        return util.format(proto, PhoneNumberUtil.PhoneNumberFormat.E164)
    }

    override fun isValid(raw: String, region: String?): Boolean = try {
        normalizeToE164(raw, region); true
    } catch (e: Exception) {
        false
    }

    override fun maskE164(e164: String): String {
        // +1XXXXXXXXXX => +1***XXXXXXX; general: keep country code and last 2-4 digits
        return try {
            val proto = util.parse(e164, null)
            val national = proto.nationalNumber.toString()
            val keep = national.takeLast(minOf(4, national.length))
            val masked = "*".repeat(maxOf(0, national.length - keep.length)) + keep
            "+${proto.countryCode}$masked"
        } catch (e: Exception) {
            // fallback simple mask
            if (e164.length <= 4) "****" else e164.take(2) + "***" + e164.takeLast(2)
        }
    }
}
