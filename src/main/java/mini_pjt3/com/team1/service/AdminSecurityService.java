package mini_pjt3.com.team1.service;

import mini_pjt3.com.team1.dto.response.*;
import mini_pjt3.com.team1.entity.*;
import mini_pjt3.com.team1.enums.*;
import mini_pjt3.com.team1.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
/**
     * 컨트롤러(AdminSecurityController)에서 호출하는 메서드명으로 통일
     * 최근 3시간 시간대별 위협 추이 데이터 생성
     */
    public List<Map<String, Object>> getRecent3HourThreatTrend() {
        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 최근 3시간 (현재 시각 포함 0, 1, 2시간 전) 데이터를 수집
        for (int i = 2; i >= 0; i--) {
            LocalDateTime hourStart = now.minusHours(i).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime hourEnd = hourStart.plusMinutes(59).plusSeconds(59).withNano(999999999);
            
            // 보안 위반 로그 건수 카운트
            long count = securityViolationLogRepository.countByCreatedAtBetween(hourStart, hourEnd);
            
            Map<String, Object> data = new HashMap<>();
            data.put("time", hourStart.getHour() + "시");
            data.put("threat", count);
            trendData.add(data);
        }
        return trendData;
    }

    /**
     * 대시보드 상단 요약 정보 및 실시간 마스킹 감사 차트 데이터 조회
     */
    public AdminSecuritySummaryResponse getSummary() {
        // 1. 마스킹 성공률 데이터 조회
        List<MaskingAuditLog> allLogs = maskingAuditLogRepository.findAll();
        long totalAuditCount = allLogs.size();

        long successCount = allLogs.stream()
                .filter(l -> l.getResult() == AuditResult.SUCCESS).count();

        double successRate = totalAuditCount == 0
                ? 0.0
                : Math.round((successCount * 1000.0) / totalAuditCount) / 10.0;

        //항목별 '*' 미포함(실패) 건수 실시간 계산 로직
        long accountFails = allLogs.stream()
                .filter(l -> l.getMaskedAccountNumber() != null && !l.getMaskedAccountNumber().contains("*")).count();
        long nameFails = allLogs.stream()
                .filter(l -> l.getMaskedName() != null && !l.getMaskedName().contains("*")).count();
        long emailFails = allLogs.stream()
                .filter(l -> l.getMaskedEmail() != null && !l.getMaskedEmail().contains("*")).count();
        long phoneFails = allLogs.stream()
                .filter(l -> l.getMaskedPhone() != null && !l.getMaskedPhone().contains("*")).count();

        // 항목별 성공률 산출
        double accountRate = totalAuditCount == 0 ? 100.0 : Math.round(((totalAuditCount - accountFails) * 1000.0) / totalAuditCount) / 10.0;
        double nameRate = totalAuditCount == 0 ? 100.0 : Math.round(((totalAuditCount - nameFails) * 1000.0) / totalAuditCount) / 10.0;
        double emailRate = totalAuditCount == 0 ? 100.0 : Math.round(((totalAuditCount - emailFails) * 1000.0) / totalAuditCount) / 10.0;
        double phoneRate = totalAuditCount == 0 ? 100.0 : Math.round(((totalAuditCount - phoneFails) * 1000.0) / totalAuditCount) / 10.0;

        // 2. '차단된 접근' 카드용
        long totalViolations = securityViolationLogRepository.count();

        // 3. '고위험 위협' 카드용
        long openAlertCount = anomalyAlertRepository.countByStatus(AlertStatus.OPEN);

        // 4. 최근 24시간 관리자 접속 건수
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long adminAccessLast24Hours = adminAccessLogRepository.countByCreatedAtAfter(last24Hours);

        // DTO 순서에 맞춰서 리턴
        return new AdminSecuritySummaryResponse(
                totalAuditCount,
                successRate,
                accountRate,
                nameRate,
                emailRate,
                phoneRate,
                totalViolations,
                openAlertCount,
                adminAccessLast24Hours
        );
    }

    public long getTotalAccessLogCount() {
        return securityViolationLogRepository.count();
    }

    @Transactional
    public int runMaskingAudit() {
        List<VirtualAccount> accounts = virtualAccountRepository.findAllByPaymentStatusForAudit(TransactionStatus.PAID);

        for (VirtualAccount account : accounts) {
            Payment payment = account.getPayment();
            Long paymentId = payment.getId();
            Long virtualAccountId = account.getId();
            Member member = payment.getMember();

            String maskedAccountNumber = account.getMaskedAccountNumber();
            String maskedName = AdminAccountResponse.maskName(member.getName());
            String maskedPhone = AdminAccountResponse.maskPhone(member.getPhone());
            String maskedEmail = AdminAccountResponse.maskEmail(member.getEmail());

            boolean isAllValid = isValidMaskedAccount(maskedAccountNumber)
                    && isValidMaskedName(maskedName)
                    && isValidMaskedPhone(maskedPhone)
                    && isValidMaskedEmail(maskedEmail);

            if (isAllValid) {
                maskingAuditLogRepository.save(
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

    private boolean isValidMaskedName(String maskedName) {
        return maskedName != null && !maskedName.contains("알 수 없음") && maskedName.contains("*");
    }

    private boolean isValidMaskedPhone(String maskedPhone) {
        return maskedPhone != null && !maskedPhone.contains("정보 없음") && maskedPhone.contains("*");
    }

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
     * 오래된 보안 로그 정리 (수동 호출 및 스케줄러용)
     */
    @Transactional
    public void cleanOldAuditLogs() {
        // 보관 주기 설정 (예: 90일 이전 데이터 삭제)
        LocalDateTime retentionPeriod = LocalDateTime.now().minusDays(90);
        
        maskingAuditLogRepository.deleteByCreatedAtBefore(retentionPeriod);
        adminAccessLogRepository.deleteByCreatedAtBefore(retentionPeriod);
        securityViolationLogRepository.deleteByCreatedAtBefore(retentionPeriod);
        
        System.out.println("보안 로그 정리가 완료되었습니다. (기준일: " + retentionPeriod + ")");
    }
}