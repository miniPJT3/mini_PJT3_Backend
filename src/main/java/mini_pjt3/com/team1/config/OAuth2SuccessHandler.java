package mini_pjt3.com.team1.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException {
        
        // 1. JWT 토큰 생성 (메서드명 createToken으로 일치 확인)
        String token = jwtUtil.createToken(authentication); 

        // 2. HTTP-Only 쿠키 생성
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(3600)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        //사용자 권한(Role)에 따른 리다이렉트 경로 설정
        // 권한 정보를 가져와서 판매자(SELLER)면 관리자 대시보드로 보냅니다.
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        String targetUrl = "http://localhost:5173/";

        if ("ROLE_SELLER".equals(role)) {
            targetUrl = "http://localhost:5173/admin/dashboard";
        } else if ("ROLE_USER".equals(role)) {
            targetUrl = "http://localhost:5173/dashboard"; 
        }

        response.sendRedirect(targetUrl); 
    }
}