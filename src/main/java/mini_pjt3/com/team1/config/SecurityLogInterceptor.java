package mini_pjt3.com.team1.config;

import mini_pjt3.com.team1.controller.SseController; // 이 경로가 맞는지 확인!
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

    private final SseController sseController;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        int status = response.getStatus();

        if (status == 403 || status == 429) {
            String uri = request.getRequestURI();
            log.warn("보안 위협 감지: {} | 상태코드: {}", uri, status);
            
            // 실시간 알림 전송
            sseController.broadcastSecurityAlert("위험 감지: " + uri + " (" + status + ")");
        }
    }
}