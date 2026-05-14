package mini_pjt3.com.team1.controller;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.response.*;
import mini_pjt3.com.team1.service.AdminSecurityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/security")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor // 생성자 주입을 Lombok으로 대체하여 깔끔하게 변경
public class AdminSecurityController {

    private final AdminSecurityService adminSecurityService;

    /**
     * [대시보드 상단 요약 정보]
     */
    @GetMapping("/summary")
    public AdminSecuritySummaryResponse getSummary() {
        return adminSecurityService.getSummary();
    }

    /**
     * [차단된 접근 누적 카운트]
     */
    @GetMapping("/violations/count")
    public ResponseEntity<Long> getAdminAccessLogsCount() {
        return ResponseEntity.ok(adminSecurityService.getTotalAccessLogCount());
    }

    /**
     * [실시간 보안 위반 로그 리스트]
     */
    @GetMapping("/violations")
    public List<SecurityViolationResponse> getSecurityViolationLogs() {
        return adminSecurityService.getRecentSecurityViolationLogs();
    }

    /**
     * [마스킹 감사 실행]
     */
    @PostMapping("/masking-audits/run")
    public Map<String, Integer> runMaskingAudit() {
        int checkedCount = adminSecurityService.runMaskingAudit();
        return Map.of("checkedCount", checkedCount);
    }

    /**
     * [마스킹 감사 이력 조회]
     */
    @GetMapping("/masking-audits")
    public List<MaskingAuditResponse> getMaskingAuditLogs() {
        return adminSecurityService.getRecentMaskingAuditLogs();
    }

    /**
     * [관리자 접근 로그 조회]
     */
    @GetMapping("/access-logs")
    public List<AdminAccessLogResponse> getAdminAccessLogs() {
        return adminSecurityService.getRecentAdminAccessLogs();
    }

    /**
     * [시간별 위반 통계]
     */
    @GetMapping("/violations/hourly")
    public List<ViolationHourlyStatResponse> getHourlyViolationStats() {
        return adminSecurityService.getHourlyViolationStats();
    }

    /**
     * [보안 알림 리스트]
     */
    @GetMapping("/alerts")
    public List<AnomalyAlertResponse> getAlerts() {
        return adminSecurityService.getRecentAlerts();
    }

    /**
     * [알림 읽음 처리]
     */
    @PatchMapping("/alerts/{alertId}/read")
    public AnomalyAlertResponse markAlertRead(@PathVariable Long alertId) {
        return adminSecurityService.markAlertRead(alertId);
    }

    /**
     * [보안 로그 수동 정리]
     *
     */
    @DeleteMapping("/logs/cleanup")
    public ResponseEntity<Map<String, String>> cleanupLogs() {
        adminSecurityService.cleanOldAuditLogs();
        return ResponseEntity.ok(Map.of("message", "오래된 보안 로그 정리가 완료되었습니다."));
    }
}