package mini_pjt3.com.team1.service;

import mini_pjt3.com.team1.repository.MaskingAuditLogRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class AdminAnomalyScheduler {

    private final AdminSecurityService adminSecurityService;

    public AdminAnomalyScheduler(AdminSecurityService adminSecurityService, MaskingAuditLogRepository maskingAuditLogRepository) {
        this.adminSecurityService = adminSecurityService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void detectPaymentFailureSpike() {
        adminSecurityService.detectPaymentFailureSpike();
    }

    @Scheduled(cron = "0 0 16 * * *")
    public void runLogCleanup() {
        adminSecurityService.cleanOldAuditLogs();
    }
}