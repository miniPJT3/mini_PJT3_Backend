package mini_pjt3.com.team1.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * [1] 정적 리소스 보안 필터 제외
     * SSE는 ignoring() 보다는 filterChain 내부에서 permitAll()로 관리하는 것이 
     * CORS 필터를 태우기에 더 안정적입니다.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .requestMatchers("/assets/**", "/css/**", "/js/**", "/favicon.ico");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CORS 설정 적용 (최우선)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 2. CSRF 비활성화
            .csrf(csrf -> csrf.disable())

            // 3. 세션 사용 안 함 (Stateless)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 4. 에러 핸들링: 인증 실패 시 401 에러 반환
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(401, "Unauthorized");
                })
            )

            // 5. URL별 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", 
                    "/login/**", 
                    "/oauth2/**", 
                    "/api/auth/**", 
                    "/api/sse/**",     // SSE 허용
                    "/api/test/**", 
                    "/api/dashboard/**", 
                    "/api/admin/**", 
                    "/api/payments/**", 
                    "/api/products/**"
                ).permitAll()
                .anyRequest().authenticated()
            )

            // 6. OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2SuccessHandler)
            );

        return http.build();
    }

    /**
     * [2] CORS 세부 설정
     * SSE 통신을 위해 허용 Origin 및 ExposedHeaders를 명확히 정의합니다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 프론트엔드 주소 허용 (패턴 사용 권장)
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:5173"));
        
        // GET, POST 등 메서드와 함께 특히 OPTIONS(Preflight)가 중요합니다.
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 쿠키 및 인증 정보 포함 허용
        configuration.setAllowCredentials(true);
        
        // [중요] 브라우저가 SSE 응답의 Content-Type 등을 읽을 수 있도록 노출
        configuration.setExposedHeaders(Arrays.asList(
            "Content-Type", 
            "Cache-Control", 
            "Connection", 
            "Transfer-Encoding"
        ));
        
        // 프리플라이트 요청 캐싱
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}