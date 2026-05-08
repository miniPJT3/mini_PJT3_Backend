package mini_pjt3.com.team1.enums;

public enum TransactionStatus {
    PENDING,   // 결제 대기 중 (가상계좌 발급 직후)
    PAID,      // 입금 확인 완료
    FAILED,    // 결제 실패
    EXPIRED    // 시간 초과로 인한 만료
}