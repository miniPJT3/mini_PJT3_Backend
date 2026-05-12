package mini_pjt3.com.team1.enums;

public enum ViolationType {
    UNAUTHORIZED_ACCESS, // 미인증 접근
    FORBIDDEN_ACCESS,    // 권한 부족
    REPEATED_REQUEST,    // 반복 요청
    PAYMENT_FAILURE_SPIKE // 결제 실패 급증
}