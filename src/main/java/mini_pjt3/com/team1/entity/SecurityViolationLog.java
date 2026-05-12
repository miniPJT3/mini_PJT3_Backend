package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mini_pjt3.com.team1.enums.ViolationType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
@Table(
        name = "security_violation_logs",
        indexes = {
                @Index(name = "idx_security_violation_ip", columnList = "ip_address"),
                @Index(name = "idx_security_violation_type", columnList = "violation_type"),
                @Index(name = "idx_security_violation_created_at", columnList = "created_at")
        }
)
public class SecurityViolationLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    @Column(name = "request_method", nullable = false, length = 20)
    private String requestMethod;

    @Column(name = "request_path", nullable = false, length = 500)
    private String requestPath;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "violation_type", nullable = false, length = 50)
    private ViolationType violationType;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String message;

    // 빌더 패턴 적용
    @Builder
    private SecurityViolationLog(
            String ipAddress,
            String requestMethod,
            String requestPath,
            int statusCode,
            ViolationType violationType,
            String userAgent,
            String message
    ) {
        this.ipAddress = ipAddress;
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
        this.statusCode = statusCode;
        this.violationType = violationType;
        this.userAgent = userAgent;
        this.message = message;
    }

    // 정적 팩토리 메서드 유지 (기존 코드와의 호환성)
    public static SecurityViolationLog of(
            String ipAddress,
            String requestMethod,
            String requestPath,
            int statusCode,
            ViolationType violationType,
            String userAgent,
            String message
    ) {
        return SecurityViolationLog.builder()
                .ipAddress(ipAddress)
                .requestMethod(requestMethod)
                .requestPath(requestPath)
                .statusCode(statusCode)
                .violationType(violationType)
                .userAgent(userAgent)
                .message(message)
                .build();
    }
}