package mini_pjt3.com.team1.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/sse")
@CrossOrigin(origins = "*") // 프론트엔드 포트가 다를 경우 CORS 허용
public class SseController {

    // 현재 연결된 클라이언트들을 저장하는 Thread-safe한 Map
    // Key: 사용자 식별자(userId 또는 sessionId), Value: 해당 사용자의 SseEmitter
    private static final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 클라이언트가 실시간 알림을 받기 위해 연결하는 엔드포인트
     * @param userId 사용자 ID (구분을 위해 사용)
     */
    @GetMapping(value = "/connect/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String userId) {
        
        // Emitter 생성 (기본 타임아웃을 1시간으로 설정)
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60);
        
        // 초기 연결 시 더미 데이터 전송 (연결 직후 데이터가 없으면 503 에러가 발생할 수 있음)
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected!"));
        } catch (IOException e) {
            log.error("SSE 연결 중 에러 발생: {}", e.getMessage());
        }

        //저장
        emitters.put(userId, emitter);

        // 완료, 타임아웃, 에러 발생 시 Map에서 제거
        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료: {}", userId);
            emitters.remove(userId);
        });
        
        emitter.onTimeout(() -> {
            log.warn("SSE 연결 타임아웃: {}", userId);
            emitters.remove(userId);
        });

        emitter.onError((e) -> {
            log.error("SSE 연결 에러: {}, 메시지: {}", userId, e.getMessage());
            emitters.remove(userId);
        });

        return emitter;
    }

    /**
     * 외부(Interceptor나 Service)에서 보안 위협 발생 시 호출할 메서드
     * 모든 접속자에게 실시간 보안 알림을 전송함
     */
    public void broadcastSecurityAlert(Object alertData) {
        log.info("보안 알림 브로드캐스팅 중...");
        
        emitters.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("security-alert") // 프론트에서 리스닝할 이벤트 이름
                        .data(alertData)        // 실제 전송할 데이터 (DTO 또는 String)
                        .reconnectTime(3000));  // 연결 끊겼을 때 재연결 시도 시간 (3초)
            } catch (IOException e) {
                // 전송 실패 시 해당 emitter 제거
                log.error("알림 전송 실패, 유저 제거: {}", id);
                emitters.remove(id);
            }
        });
    }
}