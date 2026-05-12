package mini_pjt3.com.team1.controller;

import mini_pjt3.com.team1.entity.SecurityViolationLog;
import mini_pjt3.com.team1.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SseController {

    private final SseService sseService;

    /**
     * 클라이언트 SSE 연결 엔드포인트
     */
    @GetMapping(value = "/connect/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String userId) {
        log.info("SSE 연결 시도: userId={}", userId);
        return sseService.subscribe(userId);
    }

    /**
     * DB에 저장된 모든 보안 위반 로그 조회
     * 새로고침 시 UI가 초기화되는 것을 방지하기 위해 기존 데이터를 불러옴
     */
    @GetMapping("/logs")
    public ResponseEntity<List<SecurityViolationLog>> getAllLogs() {
        log.info("기존 보안 로그 리스트 조회 요청");
        List<SecurityViolationLog> logs = sseService.getAllSecurityLogs();
        return ResponseEntity.ok(logs);
    }

    /**
     * 테스트용 알림 전송 API
     */
    @GetMapping("/test-alert")
    public String testAlert() {
        sseService.broadcastSecurityAlert("실시간 보안 점검 중입니다.");
        return "Alert sent!";
    }
}