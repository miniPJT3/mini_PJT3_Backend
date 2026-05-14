package mini_pjt3.com.team1.service;

import mini_pjt3.com.team1.dto.response.*;
import mini_pjt3.com.team1.entity.*;
import mini_pjt3.com.team1.enums.*;
import mini_pjt3.com.team1.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
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

    /**
     * 대시보드 상단 요약 정보 (4가지 카드) 조회
     */
    public AdminSecuritySummaryResponse getSummary() {
        // 1. 마스킹 성공률 데이터
        long totalAuditCount = maskingAuditLogRepository.count();
        long successCount = maskingAuditLogRepository.countByResult(AuditResult.SUCCESS);
        double successRate = totalAuditCount == 0
                ? 0.0
                : Math.round((successCount * 1000.0) / totalAuditCount) / 10.0;

        // 2. [핵심] '차단된 접근' 카드용: security_violation_logs 전체 누적 건수
        long totalViolations = securityViolationLogRepository.count(); 
        
        // 3. '고위험 위협' 카드용: 처리 전 알림 건수
        long openAlertCount = anomalyAlertRepository.countByStatus(AlertStatus.OPEN);

        // 4. 최근 24시간 관리자 접속 건수
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long adminAccessLast24Hours = adminAccessLogRepository.countByCreatedAtAfter(last24Hours);

        return new AdminSecuritySummaryResponse(
                totalAuditCount,
                successRate,
                totalViolations, 
                openAlertCount,
                adminAccessLast24Hours
        );
    }

    /**
     * 프론트엔드 누적 카운트 API 전용 메서드
     * 
     */
    public long getTotalAccessLogCount() {
        return securityViolationLogRepository.count();
    }

    @Transactional
    public int runMaskingAudit() {
        List<VirtualAccount> accounts = virtualAccountRepository.findAllByPaymentStatusForAudit(TransactionStatus.PAID);

        for (VirtualAccount account : accounts) {
            Payment payment = account.getPayment(); // 결제 정보 가져오기
            Long paymentId = payment.getId();
            Long virtualAccountId = account.getId();
            Member member = payment.getMember();

            // 1. 각 항목별 마스킹 데이터 추출
            String maskedAccountNumber = account.getMaskedAccountNumber();
            String maskedName = AdminAccountResponse.maskName(member.getName());
            String maskedPhone = AdminAccountResponse.maskPhone(member.getPhone());
            String maskedEmail = AdminAccountResponse.maskEmail(member.getEmail());

            // 2. 통합 검증 (기존 isValid 로직 확장)
            boolean isAllValid = isValidMaskedAccount(maskedAccountNumber)
                    && isValidMaskedName(maskedName)
                    && isValidMaskedPhone(maskedPhone)
                    && isValidMaskedEmail(maskedEmail);

            if (isAllValid) {
                maskingAuditLogRepository.save(
                        // 확장된 success 메서드 호출 🥊
                        MaskingAuditLog.success(paymentId, virtualAccountId,
                                maskedName, maskedAccountNumber, maskedPhone, maskedEmail)
                );
            } else {
                String reason = "개인정보 항목 중 마스킹 오류 발견 (성함/계좌/폰/이메일)";
                maskingAuditLogRepository.save(
                        MaskingAuditLog.fail(paymentId, virtualAccountId, maskedAccountNumber, reason)
                );
                createMaskingFailureAlert(paymentId, virtualAccountId, reason);
            }
        }
        return accounts.size();
    }
    // 성함 마스킹 검증
    private boolean isValidMaskedName(String maskedName) {
        return maskedName != null && !maskedName.contains("알 수 없음") && maskedName.contains("*");
    }

    // 전화번호 마스킹 검증
    private boolean isValidMaskedPhone(String maskedPhone) {
        return maskedPhone != null && !maskedPhone.contains("정보 없음") && maskedPhone.contains("*");
    }

    // 이메일 마스킹 검증
    private boolean isValidMaskedEmail(String maskedEmail) {
        return maskedEmail != null && !maskedEmail.contains("정보 없음") && maskedEmail.contains("*") && maskedEmail.contains("@");
    }

    private boolean isValidMaskedAccount(String maskedAccountNumber) {
        return maskedAccountNumber != null && !maskedAccountNumber.isBlank() && maskedAccountNumber.contains("*");
    }

    private void createMaskingFailureAlert(Long paymentId, Long virtualAccountId, String reason) {
        anomalyAlertRepository.save(AnomalyAlert.open(
                AlertLevel.CRITICAL, "데이터 마스킹 실패 탐지", null, 1L,
                "virtualAccountId=" + virtualAccountId + ", 사유=" + reason
        ));
    }

    public List<MaskingAuditResponse> getRecentMaskingAuditLogs() {
        return maskingAuditLogRepository.findTop50ByOrderByCreatedAtDesc().stream().map(MaskingAuditResponse::from).toList();
    }

    public List<AdminAccessLogResponse> getRecentAdminAccessLogs() {
        return adminAccessLogRepository.findTop20ByOrderByCreatedAtDesc().stream().map(AdminAccessLogResponse::from).toList();
    }

    public List<SecurityViolationResponse> getRecentSecurityViolationLogs() {
        // Repository에 추가한 findTop20ByOrderByCreatedAtDesc 사용
        return securityViolationLogRepository.findTop20ByOrderByCreatedAtDesc().stream().map(SecurityViolationResponse::from).toList();
    }

    public List<ViolationHourlyStatResponse> getHourlyViolationStats() {
        LocalDateTime from = LocalDateTime.now().minusHours(24);
        return securityViolationLogRepository.countHourlyViolations(from)
                .stream()
                .map(row -> new ViolationHourlyStatResponse(String.valueOf(row[0]), ((Number) row[1]).longValue()))
                .toList();
    }

    public List<AnomalyAlertResponse> getRecentAlerts() {
        return anomalyAlertRepository.findTop50ByOrderByCreatedAtDesc().stream().map(AnomalyAlertResponse::from).toList();
    }

    @Transactional
    public AnomalyAlertResponse markAlertRead(Long alertId) {
        AnomalyAlert alert = anomalyAlertRepository.findById(alertId)
                .orElseThrow(() -> new NoSuchElementException("알림 ID " + alertId + "를 찾을 수 없습니다."));
        alert.markRead();
        return AnomalyAlertResponse.from(alert);
    }

    @Transactional
    public void recordAdminAccess(String username, String ipAddress, String method, String path, int statusCode, String userAgent) {
        adminAccessLogRepository.save(AdminAccessLog.of(username, ipAddress, method, path, statusCode, userAgent));
    }

    @Transactional
    public void recordViolation(String ipAddress, String method, String path, int statusCode, String userAgent) {
        ViolationType violationType = (statusCode == 401) ? ViolationType.UNAUTHORIZED_ACCESS : ViolationType.FORBIDDEN_ACCESS;
        securityViolationLogRepository.save(SecurityViolationLog.of(ipAddress, method, path, statusCode, violationType, userAgent, "비인가 접근 차단"));
        detectRepeatedIpAccess(ipAddress);
    }

    private void detectRepeatedIpAccess(String ipAddress) {
        LocalDateTime from = LocalDateTime.now().minusMinutes(REPEATED_IP_WINDOW_MINUTES);
        long recentCount = securityViolationLogRepository.countByIpAddressAndCreatedAtAfter(ipAddress, from);
        if (recentCount < REPEATED_IP_THRESHOLD) return;

        String title = "동일 IP 반복 차단 탐지";
        if (anomalyAlertRepository.existsBySourceIpAndTitleAndStatusAndCreatedAtAfter(ipAddress, title, AlertStatus.OPEN, from)) return;

        anomalyAlertRepository.save(AnomalyAlert.open(AlertLevel.WARNING, title, ipAddress, recentCount, "비정상 반복 접근 발생"));
    }

    @Transactional
    public void detectPaymentFailureSpike() {
        LocalDateTime from = LocalDateTime.now().minusMinutes(PAYMENT_FAILURE_WINDOW_MINUTES);
        long failedCount = paymentRepository.countByStatusAndCreatedAtAfter(TransactionStatus.FAILED, from);
        if (failedCount < PAYMENT_FAILURE_THRESHOLD) return;

        String title = "결제 실패율 급증";
        if (anomalyAlertRepository.existsByTitleAndStatusAndCreatedAtAfter(title, AlertStatus.OPEN, from)) return;

        anomalyAlertRepository.save(AnomalyAlert.open(AlertLevel.CRITICAL, title, null, failedCount, "단기간 내 결제 실패 다량 발생"));
    }

    /**
     * [보안 로그 정리] 2일 지난 로그 삭제
     * 스케줄러(오후 4시)와 컨트롤러에서 공통으로 사용
     */
    @Transactional
    public void cleanOldAuditLogs() {
        LocalDateTime retentionPeriod = LocalDateTime.now().minusDays(2);

        maskingAuditLogRepository.deleteByCreatedAtBefore(retentionPeriod);
        adminAccessLogRepository.deleteByCreatedAtBefore(retentionPeriod);
        securityViolationLogRepository.deleteByCreatedAtBefore(retentionPeriod);

        System.out.println("보안 로그 정리가 완료되었습니다.");
    }
}