package mini_pjt3.com.team1.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SecurityLogInterceptor securityLogInterceptor;

    /**
     * 프론트엔드(React) 서버의 접속을 허용하는 CORS 설정입니다.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:5173") // allowedOrigins보다 유연한 패턴 사용 가능
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                // SSE 통신 시 클라이언트가 읽어야 할 헤더를 명시적으로 노출
                .exposedHeaders("Content-Type", "Cache-Control", "Connection") 
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 작성한 보안 로그 인터셉터를 등록합니다.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityLogInterceptor)
                .addPathPatterns("/**") 
                // SSE 연결 요청과 정적 리소스를 제외하여 실시간 스트림 끊김을 방지합니다.
                .excludePathPatterns(
                    "/api/sse/**", 
                    "/assets/**", 
                    "/css/**", 
                    "/js/**", 
                    "/favicon.ico",
                    "/error"
                ); 
    }
}