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
        
        // JWT 토큰 생성
        String token = jwtUtil.createToken(authentication); 

        //HTTP-Only 쿠키 생성 (보안 설정)
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .path("/")
                .httpOnly(true)    // JavaScript 접근 차단
                .secure(false)     // 로컬 환경(http)에서는 false, 배포(https) 시 true
                .maxAge(3600)      // 1시간 유지
                .sameSite("Lax")   // 크로스 사이트 요청 설정
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        //DB Role 기반 리다이렉트 경로 설정
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        String targetUrl = "http://localhost:5173";

        // DB의 role 값에 따라 목적지 분기 처리
        if ("ROLE_ADMIN".equals(role)) {
            // ADMIN 권한: 관리자 대시보드
            targetUrl += "/admin/dashboard";
        } else if ("ROLE_SELLER".equals(role)) {
            // SELLER 권한: 판매자 대시보드
            targetUrl += "/seller";
        } else if ("ROLE_USER".equals(role)) {
            // USER 권한: 일반 사용자 대시보드
            targetUrl += "/user";
        } else {
            // 기본 경로 (로그인 페이지 등)
            targetUrl += "/login";
        }

        response.sendRedirect(targetUrl); 
    }
}