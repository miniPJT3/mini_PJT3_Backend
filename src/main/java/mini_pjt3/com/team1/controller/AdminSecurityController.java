package mini_pjt3.com.team1.controller;

import mini_pjt3.com.team1.dto.response.*;
import mini_pjt3.com.team1.service.AdminSecurityService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/security")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSecurityController {

    private final AdminSecurityService adminSecurityService;

    public AdminSecurityController(AdminSecurityService adminSecurityService) {
        this.adminSecurityService = adminSecurityService;
    }

    @GetMapping("/summary")
    public AdminSecuritySummaryResponse getSummary() {
        return adminSecurityService.getSummary();
    }

    @PostMapping("/masking-audits/run")
    public Map<String, Integer> runMaskingAudit() {
        int checkedCount = adminSecurityService.runMaskingAudit();
        return Map.of("checkedCount", checkedCount);
    }

    @GetMapping("/masking-audits")
    public List<MaskingAuditResponse> getMaskingAuditLogs() {
        return adminSecurityService.getRecentMaskingAuditLogs();
    }

    @GetMapping("/access-logs")
    public List<AdminAccessLogResponse> getAdminAccessLogs() {
        return adminSecurityService.getRecentAdminAccessLogs();
    }

    @GetMapping("/violations")
    public List<SecurityViolationResponse> getSecurityViolationLogs() {
        return adminSecurityService.getRecentSecurityViolationLogs();
    }

    @GetMapping("/violations/hourly")
    public List<ViolationHourlyStatResponse> getHourlyViolationStats() {
        return adminSecurityService.getHourlyViolationStats();
    }

    @GetMapping("/alerts")
    public List<AnomalyAlertResponse> getAlerts() {
        return adminSecurityService.getRecentAlerts();
    }

    @PatchMapping("/alerts/{alertId}/read")
    public AnomalyAlertResponse markAlertRead(@PathVariable Long alertId) {
        return adminSecurityService.markAlertRead(alertId);
    }
}