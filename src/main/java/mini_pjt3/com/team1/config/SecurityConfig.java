package mini_pjt3.com.team1.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.entity.SecurityViolationLog;
import mini_pjt3.com.team1.enums.ViolationType;
import mini_pjt3.com.team1.service.SseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final SseService sseService; // SSE 실시간 알림 및 DB 저장을 위해 주입
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // 🥊 추가: 인증 필터 주입

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // CSRF 비활성화
            .csrf(csrf -> csrf.disable())

            // 세션 관리: STATELESS
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 예외 핸들링 (중요: 여기서 실시간 보안 알림을 호출합니다)
            .exceptionHandling(exception -> exception
                // 인증되지 않은 사용자가 보호된 리소스에 접근했을 때 (401)
                .authenticationEntryPoint((request, response, authException) -> {
                    SecurityViolationLog violationLog = SecurityViolationLog.of(
                            getClientIp(request),
                            request.getMethod(),
                            request.getRequestURI(),
                            HttpServletResponse.SC_UNAUTHORIZED,
                            ViolationType.UNAUTHORIZED_ACCESS,
                            request.getHeader("User-Agent"),
                            "미인증 접근 시도 감지: " + request.getRequestURI()
                    );

                    sseService.sendAlert(violationLog); // DB 저장 및 SSE 전송
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                })
                // 권한이 없는 사용자가 접근했을 때 (403)
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    SecurityViolationLog violationLog = SecurityViolationLog.of(
                            getClientIp(request),
                            request.getMethod(),
                            request.getRequestURI(),
                            HttpServletResponse.SC_FORBIDDEN,
                            ViolationType.FORBIDDEN_ACCESS,
                            request.getHeader("User-Agent"),
                            "보안 위반 감지: 권한 부족 (" + request.getRequestURI() + ")"
                    );

                    sseService.sendAlert(violationLog); // DB 저장 및 SSE 전송
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                })
            )

            // URL별 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/login/**",
                    "/oauth2/**",
                    "/api/auth/**",
                    "/api/sse/**", // SSE 연결은 무조건 허용
                    "/api/test/**",
                    "/api/dashboard/**",
                    "/api/products/**",
                    "/assets/**", "/css/**", "/js/**", "/favicon.ico", "/error"
                ).permitAll()

                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/payments/**").hasAnyRole("USER", "ADMIN", "SELLER")

                .anyRequest().authenticated()
            )



            // OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2SuccessHandler)
            )
                // 🥊 [핵심] JWT 인증 필터를 시큐리티 필터 체인에 등록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);



        return http.build();
    }

    // CORS 세부 설정: 프론트엔드(5173)의 요청과 쿠키 전송을 허용
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "Cache-Control", "Connection", "Transfer-Encoding"
        ));

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 클라이언트 IP 추출 헬퍼 메서드
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.contains(",") ? ip.split(",")[0] : ip;
    }
}