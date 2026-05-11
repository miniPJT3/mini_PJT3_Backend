package mini_pjt3.com.team1.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

            // CORS 설정 적용
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))


            //CSRF 비활성화 (Stateless 방식이므로 비활성화가 일반적)
            .csrf(csrf -> csrf.disable())

            //세션 사용 안 함 (JWT 기반 개발을 위한 설정)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // URL별 권한 설정
            .authorizeHttpRequests(auth -> auth
                // "/", 로그인, OAuth2 관련 경로와 "일반 회원가입(/api/auth/**)" 경로를 허용합니다.
                .requestMatchers("/", "/login/**", "/oauth2/**", "/api/auth/**").permitAll()
                // H2 콘솔을 사용하면 아래 주석을 해제
                // .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated() // 나머지는 인증 필요
            )

            //OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2SuccessHandler)
            );


        // http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    // CORS 세부 설정: 프론트엔드(5173)의 요청과 쿠키 전송을 허용
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}