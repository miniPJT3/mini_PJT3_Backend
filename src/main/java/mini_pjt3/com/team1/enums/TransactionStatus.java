package mini_pjt3.com.team1.enums;

public enum TransactionStatus {
    PENDING,   // 결제 대기 중 (가상계좌 발급 직후)
    DEPOSITED,  // 사용자가 입금 완료 보고함 (확인 중)
    PAID,       // 판매자가 최종 승인함 (결제 완료)
    FAILED,    // 결제 실패
    EXPIRED    // 시간 초과로 인한 만료
}