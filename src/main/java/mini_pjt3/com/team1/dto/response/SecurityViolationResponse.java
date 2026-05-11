package mini_pjt3.com.team1.dto.response;

import mini_pjt3.com.team1.entity.SecurityViolationLog;
import mini_pjt3.com.team1.enums.ViolationType;

import java.time.LocalDateTime;

public record SecurityViolationResponse(
        Long id,
        String ipAddress,
        String requestMethod,
        String requestPath,
        int statusCode,
        ViolationType violationType,
        String userAgent,
        String message,
        LocalDateTime createdAt
) {
    public static SecurityViolationResponse from(SecurityViolationLog log) {
        return new SecurityViolationResponse(
                log.getId(),
                log.getIpAddress(),
                log.getRequestMethod(),
                log.getRequestPath(),
                log.getStatusCode(),
                log.getViolationType(),
                log.getUserAgent(),
                log.getMessage(),
                log.getCreatedAt()
        );
    }
}