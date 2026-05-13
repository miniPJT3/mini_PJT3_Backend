package mini_pjt3.com.team1.config;

import mini_pjt3.com.team1.entity.SecurityViolationLog;
import mini_pjt3.com.team1.enums.ViolationType;
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
        String uri = request.getRequestURI();

        // 시큐리티가 처리하지 못하는 404와 429만 인터셉터가 담당!
        if ((status == 404 || status == 429) && !uri.equals("/error")) {

            // IP 추출 (X-Forwarded-For 헤더 확인 로직 포함)
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            String clientIp = ip.contains(",") ? ip.split(",")[0] : ip;

            SecurityViolationLog violationLog = SecurityViolationLog.of(
                    clientIp,
                    request.getMethod(),
                    uri,
                    status,
                    status == 429 ? ViolationType.REPEATED_REQUEST : ViolationType.UNAUTHORIZED_ACCESS,
                    request.getHeader("User-Agent"),
                    status == 429 ? "과도한 요청 감지: " + uri : "존재하지 않는 경로 접근: " + uri
            );

            sseService.sendAlert(violationLog);
        }
    }
}