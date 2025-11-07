package me.hyunlee.order.domain.model.vo

@JvmInline
value class BagCount(val value: Int) {
    init { require(value in 1..10) { "가방 수는 1~10 사이여야 함" } }
    companion object { fun create(value: Int) = BagCount(value) }
}