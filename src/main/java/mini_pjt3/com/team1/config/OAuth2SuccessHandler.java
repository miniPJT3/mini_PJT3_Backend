package mini_pjt3.com.team1.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.config.JwtUtil; // 실제 패키지 경로에 맞게 수정 확인
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil; // 주입 받아 사용

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException {
        
        // 실제 JWT 토큰 생성 (주석 해제 및 실제 메서드 호출)
        // authentication 객체에서 유저 정보를 추출하여 토큰을 만듦.
        String token = jwtUtil.createToken(authentication); 

        // HTTP-Only 쿠키 생성
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .path("/")
                .httpOnly(true)    // JavaScript에서 접근 불가 (XSS 공격 방지)
                .secure(false)    // 로컬 http 환경에선 false, 배포(https) 시 true로 변경 필요
                .maxAge(3600)      // 1시간 유지
                .sameSite("Lax")   // 크로스 사이트 요청 설정 (OAuth2 리다이렉트 대응)
                .build();

        //응답 헤더에 쿠키 추가
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 프론트엔드 페이지로 이동
        // 로그인이 성공하면 보통 메인 페이지나 대시보드로 이동.
        response.sendRedirect("http://localhost:5173/"); 
    }
}