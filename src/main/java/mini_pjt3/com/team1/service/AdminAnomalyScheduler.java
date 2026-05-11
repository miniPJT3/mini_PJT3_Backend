package mini_pjt3.com.team1.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AdminAnomalyScheduler {

    private final AdminSecurityService adminSecurityService;

    public AdminAnomalyScheduler(AdminSecurityService adminSecurityService) {
        this.adminSecurityService = adminSecurityService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void detectPaymentFailureSpike() {
        adminSecurityService.detectPaymentFailureSpike();
    }
}