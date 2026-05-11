package mini_pjt3.com.team1.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter // 이 어노테이션이 있어야 getLoginId() 등을 사용할 수 있습니다.
public class LoginRequest {
    private String loginId;
    private String password;
}