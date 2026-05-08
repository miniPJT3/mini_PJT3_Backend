package mini_pjt3.com.team1.enums;

public enum AccountStatus {
    ACTIVE,    // 사용 가능 (입금 대기)
    USED,      // 입금 완료되어 사용 종료
    EXPIRED    // 3시간 경과로 인한 무효화
}