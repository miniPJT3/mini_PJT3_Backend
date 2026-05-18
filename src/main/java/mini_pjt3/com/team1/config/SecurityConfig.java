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
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final SseService sseService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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

                // 예외 핸들링
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            String userAgent = request.getHeader("User-Agent");
                            String uri = request.getRequestURI();

                            boolean isExempted = uri.equals("/error")
                                    || uri.equals("/api/auth/logout")
                                    || uri.equals("/api/health")
                                    || (userAgent != null && userAgent.contains("ELB-HealthChecker"));

                            if (!isExempted) {
                                SecurityViolationLog violationLog = SecurityViolationLog.of(
                                        getClientIp(request),
                                        request.getMethod(),
                                        uri,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        ViolationType.UNAUTHORIZED_ACCESS,
                                        userAgent,
                                        "미인증 접근 시도 감지: " + uri
                                );
                                sseService.sendAlert(violationLog);
                            }
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
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
                            sseService.sendAlert(violationLog);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                        })
                )

                // URL별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/oauth2/**",
                                "/api/auth/**",
                                "/api/sse/**",
                                "/api/test/**",
                                "/api/products/**",
                                "/api/dashboard/**",
                                "/api/member/me",
                                "/api/member/additional-info",
                                "/assets/**", "/css/**", "/js/**", "/favicon.ico", "/error", "/api/health"
                        ).permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/payments/**").hasAnyRole("USER", "ADMIN", "SELLER")

                        .anyRequest().authenticated()
                )

                // OAuth2 로그인 설정 수정
                .oauth2Login(oauth2 -> oauth2
                        // 💡 [핵심 추가]: 프론트의 /api/oauth2/authorization/google 주소를 가로채도록 엔드포인트 지정
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/api/oauth2/authorization")
                        )
                        // 구글 인증 완료 후 백엔드가 콜백을 넘겨받는 엔드포인트 주소 구조 매핑
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/code/*")
                        )
                        .successHandler(oAuth2SuccessHandler)
                )

                // 시큐리티 기본 로그아웃 비활성화
                .logout(logout -> logout.disable())

                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://team01-alb-1090661033.ap-northeast-2.elb.amazonaws.com"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization", "Set-Cookie", "Content-Type"
        ));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.contains(",") ? ip.split(",")[0] : ip;
    }
}