//package me.hyunlee.laundry.payment.history.mongo
//
//object MaskingUtil {
//    private val sensitiveKeys = setOf(
//        "card_number", "account_number", "iban", "pan", "secret", "clientSecret",
//        "cvc", "cvv", "security_code", "bank_account"
//    )
//
//    /**
//     * JSON payload를 재귀적으로 순회하며 민감 필드를 마스킹합니다.
//     * - 키가 민감 키 목록에 해당하면 값 전체를 "***"로 치환
//     * - 숫자만 13~19자리로 보이는 PAN은 중간을 마스킹
//     * - last4는 그대로 둠
//     */
//    @Suppress("UNCHECKED_CAST")
//    fun maskPayload(input: Any?): Any? = when (input) {
//        null -> null
//        is Map<*, *> -> input.entries.associate { (k, v) ->
//            val key = k?.toString() ?: ""
//            val masked = if (key.lowercase() in sensitiveKeys.map { it.lowercase() }) {
//                "***"
//            } else if (key.equals("last4", ignoreCase = true)) {
//                v
//            } else {
//                maskPayload(v)
//            }
//            key to masked
//        }
//        is List<*> -> input.map { maskPayload(it) }
//        is String -> maskString(input)
//        else -> input
//    }
//
//    private fun maskString(s: String): String {
//        val digitsOnly = s.filter { it.isDigit() }
//        return if (digitsOnly.length in 13..19) {
//            // PAN으로 추정 → 앞 6, 뒤 4만 남기고 마스킹
//            val head = s.take(6)
//            val tail = s.takeLast(4)
//            "$head***$tail"
//        } else if (s.length > 512) {
//            s.take(512) + "…"
//        } else s
//    }
//}
