package mini_pjt3.com.team1.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * 🥊 [핵심 추가] 필터가 작동하지 않아야 할 경로 설정
     * SSE 연결이나 정적 리소스는 필터를 거치지 않게 하여 JSON 파싱 에러를 방지합니다.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/sse/") ||
                path.startsWith("/assets/") ||
                path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;
        Cookie[] cookies = request.getCookies();

        // 1. 쿠키에서 accessToken 추출
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 2. 토큰 검증 및 SecurityContext에 인증 정보 설정
        if (token != null && jwtUtil.validateToken(token)) {
            try {
                // 🥊 [핵심 수정] ROLE_USER 하드코딩 제거!
                // JwtUtil에서 토큰 내에 저장된 진짜 권한(ROLE_ADMIN 등)을 꺼내옵니다.
                Authentication auth = jwtUtil.getAuthentication(token);

                // 시큐리티 컨텍스트에 저장 (이제 컨트롤러에서 권한 체크 가능)
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.info("인증 성공: {}, 권한: {}", auth.getName(), auth.getAuthorities());
            } catch (Exception e) {
                log.error("인증 정보 설정 중 에러 발생: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}