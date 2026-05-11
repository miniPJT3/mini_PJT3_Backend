package mini_pjt3.com.team1.dto.response;

import lombok.*;

@Getter
@Builder // 이 어노테이션이 있어야 .builder() 메서드를 사용할 수 있습니다.
@AllArgsConstructor // 빌더를 사용하기 위해 모든 필드를 인자로 받는 생성자가 필요합니다.
@NoArgsConstructor // 기본 생성자도 함께 두는 것이 관례입니다.
public class MemberResponse {
    private String name;
    private String role;
    private String email;
}