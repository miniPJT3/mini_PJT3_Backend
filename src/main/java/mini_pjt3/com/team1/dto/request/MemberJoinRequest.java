package mini_pjt3.com.team1.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberJoinRequest {
    private String loginId;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String role; // 프론트에서 "USER" 또는 "SELLER" 문자열로 전달
}