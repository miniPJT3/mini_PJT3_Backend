package mini_pjt3.com.team1.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private Long id;
    private String role;
    private String username;
    private String email;
}
