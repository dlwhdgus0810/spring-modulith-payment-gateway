package me.hyunlee.order.domain.model.enums

enum class OrderStatus {
    CREATED,      // 생성(클라이언트 요청 수신, 검증 완료)
    PENDING_AUTH, // 결제 선승인 진행 대기(또는 진행 중)
    AUTHORIZED,   // 선승인 완료(캡처 대기)
    CONFIRMED,    // 픽업 예약 확정(사내 룰/슬롯 승인됨)
    PICKED_UP,    // 기사 픽업 완료
    IN_PROCESS,   // 세탁/건조/다림질 진행중
    DELIVERING,   // 배송중
    DELIVERED,    // 배송완료(종료)
    CANCELED
}