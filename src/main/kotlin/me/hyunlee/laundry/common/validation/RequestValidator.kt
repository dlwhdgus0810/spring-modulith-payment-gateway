package me.hyunlee.laundry.common.validation

/**
 * 간단한 요청 검증 콤비네이터 유틸
 * - 각 함수는 불만족 시 IllegalArgumentException을 던집니다(컨트롤러에서 400으로 매핑됨).
 */
object RequestValidator {
    fun notBlank(value: String?, name: String) {
        require(!value.isNullOrBlank()) { "$name must not be blank" }
    }

    fun startsWith(value: String, prefix: String, name: String) {
        require(value.startsWith(prefix)) { "$name must start with '$prefix'" }
    }

    fun <E : Enum<E>> oneOfEnum(value: String, enumClass: Class<E>, name: String) {
        val ok = enumClass.enumConstants.any { it.name == value }
        require(ok) { "$name must be one of ${enumClass.enumConstants.joinToString { it.name }}" }
    }

    fun intRange(value: Int?, name: String, min: Int, max: Int) {
        require(value != null && value in min..max) { "$name must be in $min..$max" }
    }

    fun yearNotBefore(value: Int?, name: String, minYear: Int) {
        require(value != null && value >= minYear) { "$name must be >= $minYear" }
    }

    fun length(value: String?, name: String, expected: Int) {
        if (value == null) return
        require(value.length == expected) { "$name must be $expected characters if provided" }
    }
}