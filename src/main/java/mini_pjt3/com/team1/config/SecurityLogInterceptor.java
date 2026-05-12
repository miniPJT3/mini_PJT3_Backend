package mini_pjt3.com.team1.config;

import mini_pjt3.com.team1.service.SseService; // SseController 대신 SseService를 임포트합니다.
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityLogInterceptor implements HandlerInterceptor {

    //서비스 계층을 직접 주입받아 사용
    private final SseService sseService;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        int status = response.getStatus();

        // 403(권한 없음), 429(너무 많은 요청) ,404(찾을 수 없음 - 보안상 위험으로 간주할 경우)등 보안 위협 상태 코드 확인
        if (status == 403 || status == 429 || status == 404) {
            String uri = request.getRequestURI();
            String method = request.getMethod();
            log.warn("보안 위협 감지: [{}] {} | 상태코드: {}", method, uri, status);
            
            // sseService를 통해 실시간 알림을 브로드캐스팅
            sseService.broadcastSecurityAlert("위험 감지: " + method + " " + uri + " (Status: " + status + ")");
        }
    }
}