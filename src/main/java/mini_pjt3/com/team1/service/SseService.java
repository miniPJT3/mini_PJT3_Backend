package mini_pjt3.com.team1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mini_pjt3.com.team1.entity.SecurityViolationLog;
import mini_pjt3.com.team1.repository.SecurityViolationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final SecurityViolationLogRepository repository;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간

    /**
     * 클라이언트 SSE 구독
     */
    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE 연결 종료 (userId: {})", userId);
            emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.warn("SSE 연결 타임아웃 (userId: {})", userId);
            emitters.remove(userId);
        });
        emitter.onError((e) -> {
            log.error("SSE 에러 발생 (userId: {}): {}", userId, e.getMessage());
            emitters.remove(userId);
        });

        // 초기 연결 확인용
        sendToClient(userId, "connect", "connected!");

        return emitter;
    }

    /**
     * DB에 저장된 모든 보안 위반 로그를 최신순으로 조회
     * SseController의 에러를 해결하고 새로고침 시 데이터 유지
     */
    public List<SecurityViolationLog> getAllSecurityLogs() {
        log.info("DB에서 모든 보안 로그를 조회합니다.");
        // 최신순 정렬을 위해 리포지토리에 해당 메서드가 구현
        return repository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 보안 위반 발생 시 DB 저장 및 실시간 알림 전송
     */
    public void sendAlert(SecurityViolationLog logData) {
        //DB 저장
        SecurityViolationLog savedLog = repository.save(logData);
        log.info("📢 보안 로그 DB 저장 완료 (ID: {})", savedLog.getId());

        //브로드캐스트
        broadcastSecurityAlert(savedLog);
    }

    /**
     * 모든 접속자에게 보안 알림 브로드캐스트
     */
    public void broadcastSecurityAlert(Object alertData) {
        if (emitters.isEmpty()) {
            log.warn("⚠️ 연결된 SSE 클라이언트가 없어 알림을 전송하지 못했습니다.");
            return;
        }
        
        emitters.keySet().forEach(userId -> {
            sendToClient(userId, "security-alert", alertData);
        });
    }

    /**
     * 특정 클라이언트에게 데이터 전송
     */
    private void sendToClient(String userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data)
                        .reconnectTime(3000));
            } catch (IOException e) {
                emitters.remove(userId);
                log.error("SSE 전송 실패로 인한 연결 해제: {}", userId);
            }
        }
    }
}