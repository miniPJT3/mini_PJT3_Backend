package mini_pjt3.com.team1.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    // Spring Security의 기본 권한 관례에 따라 ROLE_ 접두사를 사용
    USER("ROLE_USER", "사용자"),
    SELLER("ROLE_SELLER", "판매자"),
    ADMIN("ROLE_ADMIN", "관리자"),
    GUEST("ROLE_GUEST", "임시"); // 구글 최초 로그인 시 부여할 권한

    private final String key;
    private final String title;
}