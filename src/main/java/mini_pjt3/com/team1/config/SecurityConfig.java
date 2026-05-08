package mini_pjt3.com.team1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 보안 비활성화 (테스트 및 API 통신을 위해 필수)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. HTTP 기본 인증 및 폼 로그인 비활성화 (Postman 테스트 방해 금지)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // 3. 경로별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 입금 승인 API는 로그인 없이도 접근 가능하게 허용!
                        .requestMatchers("/api/v1/payments/deposit").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}