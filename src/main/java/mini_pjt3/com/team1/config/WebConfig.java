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
     * 프론트엔드(React) 서버의 접속을 허용하는 CORS 설정
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOrigins(
                        "http://localhost:5173", // 로컬 개발 환경용
                        "http://team01-alb-1090661033.ap-northeast-2.elb.amazonaws.com" // 실제 배포 환경용
                ) 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true) // 쿠키/JWT 인증 정보 포함 허용
                .maxAge(3600); // 프리플라이트 요청 캐싱 시간
    }

    /**
     * 작성한 보안 로그 인터셉터를 등록
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityLogInterceptor)
                .addPathPatterns("/**") 
                // SSE 연결 및 정적 리소스는 인터셉터 검사에서 제외합니다.
                .excludePathPatterns("/api/sse/**", "/assets/**", "/css/**", "/js/**"); 
    }
}