package me.hyunlee.order.domain.model.vo

data class Address(
    val street: String,
    val city: String,
    val state: String,     // "KS" 고정 사용 권장
    val postalCode: String,
    val secondary: String? = null,
    val instructions: String? = null
) {
    init {
        require(state.length == 2) { "state must be 2-letter state code" }
        require(postalCode.matches(Regex("""^\d{5}(-\d{4})?$"""))) { "invalid US postalCode" }
    }

    companion object {
        fun create(street: String, city: String, state: String, postalCode: String,
                   secondary: String? = null, instructions: String? = null) =
            Address(street, city, state, postalCode, secondary, instructions)
    }


}
