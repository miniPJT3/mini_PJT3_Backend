package mini_pjt3.com.team1.service;

import mini_pjt3.com.team1.dto.response.*;
import mini_pjt3.com.team1.entity.*;
import mini_pjt3.com.team1.enums.*;
import mini_pjt3.com.team1.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class AdminSecurityService {

    private static final int REPEATED_IP_THRESHOLD = 5;
    private static final int REPEATED_IP_WINDOW_MINUTES = 5;
    private static final int PAYMENT_FAILURE_THRESHOLD = 10;
    private static final int PAYMENT_FAILURE_WINDOW_MINUTES = 10;

    private final AdminAccessLogRepository adminAccessLogRepository;
    private final MaskingAuditLogRepository maskingAuditLogRepository;
    private final SecurityViolationLogRepository securityViolationLogRepository;
    private final AnomalyAlertRepository anomalyAlertRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final PaymentRepository paymentRepository;

    public AdminSecurityService(
            AdminAccessLogRepository adminAccessLogRepository,
            MaskingAuditLogRepository maskingAuditLogRepository,
            SecurityViolationLogRepository securityViolationLogRepository,
            AnomalyAlertRepository anomalyAlertRepository,
            VirtualAccountRepository virtualAccountRepository,
            PaymentRepository paymentRepository
    ) {
        this.adminAccessLogRepository = adminAccessLogRepository;
        this.maskingAuditLogRepository = maskingAuditLogRepository;
        this.securityViolationLogRepository = securityViolationLogRepository;
        this.anomalyAlertRepository = anomalyAlertRepository;
        this.virtualAccountRepository = virtualAccountRepository;
        this.paymentRepository = paymentRepository;
    }

    public AdminSecuritySummaryResponse getSummary() {
        long totalAuditCount = maskingAuditLogRepository.count();
        long successCount = maskingAuditLogRepository.countByResult(AuditResult.SUCCESS);

        double successRate = totalAuditCount == 0
                ? 0.0
                : Math.round((successCount * 1000.0) / totalAuditCount) / 10.0;

        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);

        long violationsLast24Hours = securityViolationLogRepository.countByCreatedAtAfter(last24Hours);
        long openAlertCount = anomalyAlertRepository.countByStatus(AlertStatus.OPEN);
        long adminAccessLast24Hours = adminAccessLogRepository.countByCreatedAtAfter(last24Hours);

        return new AdminSecuritySummaryResponse(
                totalAuditCount,
                successRate,
                violationsLast24Hours,
                openAlertCount,
                adminAccessLast24Hours
        );
    }

    @Transactional
    public int runMaskingAudit() {
        List<VirtualAccount> accounts = virtualAccountRepository.findAllByPaymentStatusForAudit(TransactionStatus.PAID);

        for (VirtualAccount account : accounts) {
            Long paymentId = account.getPayment().getId();
            Long virtualAccountId = account.getId();
            String maskedAccountNumber = account.getMaskedAccountNumber();

            if (isValidMaskedAccount(maskedAccountNumber)) {
                maskingAuditLogRepository.save(
                        MaskingAuditLog.success(paymentId, virtualAccountId, maskedAccountNumber)
                );
            } else {
                String reason = "결제 완료 상태인데 maskedAccountNumber가 없거나 마스킹 형식이 아닙니다.";

                maskingAuditLogRepository.save(
                        MaskingAuditLog.fail(paymentId, virtualAccountId, maskedAccountNumber, reason)
                );

                createMaskingFailureAlert(paymentId, virtualAccountId, reason);
            }
        }

        return accounts.size();
    }

    private boolean isValidMaskedAccount(String maskedAccountNumber) {
        return maskedAccountNumber != null
                && !maskedAccountNumber.isBlank()
                && maskedAccountNumber.contains("*");
    }

    private void createMaskingFailureAlert(Long paymentId, Long virtualAccountId, String reason) {
        AnomalyAlert alert = AnomalyAlert.open(
                AlertLevel.CRITICAL,
                "민감 데이터 마스킹 실패",
                null,
                1L,
                "paymentId=" + paymentId
                        + ", virtualAccountId=" + virtualAccountId
                        + ", reason=" + reason
        );

        anomalyAlertRepository.save(alert);
    }

    public List<MaskingAuditResponse> getRecentMaskingAuditLogs() {
        return maskingAuditLogRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(MaskingAuditResponse::from)
                .toList();
    }

    public List<AdminAccessLogResponse> getRecentAdminAccessLogs() {
        return adminAccessLogRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(AdminAccessLogResponse::from)
                .toList();
    }

    public List<SecurityViolationResponse> getRecentSecurityViolationLogs() {
        return securityViolationLogRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(SecurityViolationResponse::from)
                .toList();
    }

    public List<ViolationHourlyStatResponse> getHourlyViolationStats() {
        LocalDateTime from = LocalDateTime.now().minusHours(24);

        return securityViolationLogRepository.countHourlyViolations(from)
                .stream()
                .map(row -> new ViolationHourlyStatResponse(
                        String.valueOf(row[0]),
                        ((Number) row[1]).longValue()
                ))
                .toList();
    }

    public List<AnomalyAlertResponse> getRecentAlerts() {
        return anomalyAlertRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(AnomalyAlertResponse::from)
                .toList();
    }

    @Transactional
    public AnomalyAlertResponse markAlertRead(Long alertId) {
        AnomalyAlert alert = anomalyAlertRepository.findById(alertId)
                .orElseThrow(() -> new NoSuchElementException("알림을 찾을 수 없습니다. alertId=" + alertId));

        alert.markRead();

        return AnomalyAlertResponse.from(alert);
    }

    @Transactional
    public void recordAdminAccess(
            String username,
            String ipAddress,
            String method,
            String path,
            int statusCode,
            String userAgent
    ) {
        AdminAccessLog log = AdminAccessLog.of(
                username,
                ipAddress,
                method,
                path,
                statusCode,
                userAgent
        );

        adminAccessLogRepository.save(log);
    }

    @Transactional
    public void recordViolation(
            String ipAddress,
            String method,
            String path,
            int statusCode,
            String userAgent
    ) {
        ViolationType violationType = resolveViolationType(statusCode);

        SecurityViolationLog log = SecurityViolationLog.of(
                ipAddress,
                method,
                path,
                statusCode,
                violationType,
                userAgent,
                "관리자 보호 리소스 접근이 차단되었습니다."
        );

        securityViolationLogRepository.save(log);

        detectRepeatedIpAccess(ipAddress);
    }

    private ViolationType resolveViolationType(int statusCode) {
        if (statusCode == 401) {
            return ViolationType.UNAUTHORIZED;
        }

        if (statusCode == 403) {
            return ViolationType.FORBIDDEN;
        }

        return ViolationType.FORBIDDEN;
    }

    private void detectRepeatedIpAccess(String ipAddress) {
        LocalDateTime from = LocalDateTime.now().minusMinutes(REPEATED_IP_WINDOW_MINUTES);

        long recentCount = securityViolationLogRepository.countByIpAddressAndCreatedAtAfter(ipAddress, from);

        if (recentCount < REPEATED_IP_THRESHOLD) {
            return;
        }

        String title = "동일 IP 반복 접근 탐지";

        boolean alreadyExists = anomalyAlertRepository.existsBySourceIpAndTitleAndStatusAndCreatedAtAfter(
                ipAddress,
                title,
                AlertStatus.OPEN,
                from
        );

        if (alreadyExists) {
            return;
        }

        AnomalyAlert alert = AnomalyAlert.open(
                AlertLevel.WARNING,
                title,
                ipAddress,
                recentCount,
                REPEATED_IP_WINDOW_MINUTES + "분 이내에 동일 IP에서 관리자 보호 리소스 접근 차단이 "
                        + recentCount + "회 발생했습니다."
        );

        anomalyAlertRepository.save(alert);
    }

    @Transactional
    public void detectPaymentFailureSpike() {
        LocalDateTime from = LocalDateTime.now().minusMinutes(PAYMENT_FAILURE_WINDOW_MINUTES);

        long failedCount = paymentRepository.countByStatusAndCreatedAtAfter(TransactionStatus.FAILED, from);

        if (failedCount < PAYMENT_FAILURE_THRESHOLD) {
            return;
        }

        String title = "결제 실패 급증";

        boolean alreadyExists = anomalyAlertRepository.existsByTitleAndStatusAndCreatedAtAfter(
                title,
                AlertStatus.OPEN,
                from
        );

        if (alreadyExists) {
            return;
        }

        AnomalyAlert alert = AnomalyAlert.open(
                AlertLevel.CRITICAL,
                title,
                null,
                failedCount,
                PAYMENT_FAILURE_WINDOW_MINUTES + "분 이내에 결제 실패가 "
                        + failedCount + "건 발생했습니다."
        );

        anomalyAlertRepository.save(alert);
    }
}