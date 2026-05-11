package mini_pjt3.com.team1.dto.response;

import mini_pjt3.com.team1.entity.MaskingAuditLog;
import mini_pjt3.com.team1.enums.AuditResult;

import java.time.LocalDateTime;

public record MaskingAuditResponse(
        Long id,
        Long paymentId,
        Long virtualAccountId,
        String maskedAccountNumber,
        AuditResult result,
        String reason,
        LocalDateTime createdAt
) {
    public static MaskingAuditResponse from(MaskingAuditLog log) {
        return new MaskingAuditResponse(
                log.getId(),
                log.getPaymentId(),
                log.getVirtualAccountId(),
                log.getMaskedAccountNumber(),
                log.getResult(),
                log.getReason(),
                log.getCreatedAt()
        );
    }
}