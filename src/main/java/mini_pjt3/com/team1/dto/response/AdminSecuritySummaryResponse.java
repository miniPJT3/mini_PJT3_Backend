package mini_pjt3.com.team1.dto.response;

public record AdminSecuritySummaryResponse(
        long totalMaskingAuditCount,
        double maskingSuccessRate,
        double accountRate,
        double nameRate,
        double emailRate,
        double phoneRate,
        long violationsLast24Hours,
        long openAlertCount,
        long adminAccessLast24Hours
) {
}