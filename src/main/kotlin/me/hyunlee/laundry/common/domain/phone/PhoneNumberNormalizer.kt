package me.hyunlee.laundry.common.domain.phone

/**
 * Abstraction for phone number normalization/validation.
 * Implementations must return E.164 formatted strings for storage and comparison.
 */
interface PhoneNumberNormalizer {
    /** Normalize any acceptable input to E.164 (e.g., +14155552671). Throws IllegalArgumentException if invalid. */
    fun normalizeToE164(raw: String, region: String? = null): String

    /** Quick check that input can be normalized. */
    fun isValid(raw: String, region: String? = null): Boolean

    /** Mask an E.164 string for display/logging. */
    fun maskE164(e164: String): String
}
