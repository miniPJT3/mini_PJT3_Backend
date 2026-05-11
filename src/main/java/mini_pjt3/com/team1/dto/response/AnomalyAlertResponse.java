package mini_pjt3.com.team1.dto.response;

import mini_pjt3.com.team1.entity.AnomalyAlert;
import mini_pjt3.com.team1.enums.AlertLevel;
import mini_pjt3.com.team1.enums.AlertStatus;

import java.time.LocalDateTime;

public record AnomalyAlertResponse(
        Long id,
        AlertLevel level,
        AlertStatus status,
        String title,
        String sourceIp,
        Long violationCount,
        String message,
        LocalDateTime createdAt
) {
    public static AnomalyAlertResponse from(AnomalyAlert alert) {
        return new AnomalyAlertResponse(
                alert.getId(),
                alert.getLevel(),
                alert.getStatus(),
                alert.getTitle(),
                alert.getSourceIp(),
                alert.getViolationCount(),
                alert.getMessage(),
                alert.getCreatedAt()
        );
    }
}