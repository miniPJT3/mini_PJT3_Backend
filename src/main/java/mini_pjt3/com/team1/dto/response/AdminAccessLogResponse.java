package mini_pjt3.com.team1.dto.response;

import mini_pjt3.com.team1.entity.AdminAccessLog;

import java.time.LocalDateTime;

public record AdminAccessLogResponse(
        Long id,
        String username,
        String ipAddress,
        String requestMethod,
        String requestPath,
        int statusCode,
        String userAgent,
        LocalDateTime createdAt
) {
    public static AdminAccessLogResponse from(AdminAccessLog log) {
        return new AdminAccessLogResponse(
                log.getId(),
                log.getUsername(),
                log.getIpAddress(),
                log.getRequestMethod(),
                log.getRequestPath(),
                log.getStatusCode(),
                log.getUserAgent(),
                log.getCreatedAt()
        );
    }
}