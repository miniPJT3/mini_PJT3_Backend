package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import mini_pjt3.com.team1.enums.AlertLevel;
import mini_pjt3.com.team1.enums.AlertStatus;

@Entity
@Table(
        name = "anomaly_alerts",
        indexes = {
                @Index(name = "idx_anomaly_alert_status", columnList = "status"),
                @Index(name = "idx_anomaly_alert_level", columnList = "level"),
                @Index(name = "idx_anomaly_alert_source_ip", columnList = "source_ip"),
                @Index(name = "idx_anomaly_alert_created_at", columnList = "created_at")
        }
)
public class AnomalyAlert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "source_ip", length = 50)
    private String sourceIp;

    @Column(name = "violation_count")
    private Long violationCount;

    @Column(columnDefinition = "TEXT")
    private String message;

    protected AnomalyAlert() {
    }

    private AnomalyAlert(
            AlertLevel level,
            AlertStatus status,
            String title,
            String sourceIp,
            Long violationCount,
            String message
    ) {
        this.level = level;
        this.status = status;
        this.title = title;
        this.sourceIp = sourceIp;
        this.violationCount = violationCount;
        this.message = message;
    }

    public static AnomalyAlert open(
            AlertLevel level,
            String title,
            String sourceIp,
            Long violationCount,
            String message
    ) {
        return new AnomalyAlert(
                level,
                AlertStatus.OPEN,
                title,
                sourceIp,
                violationCount,
                message
        );
    }

    public void markRead() {
        this.status = AlertStatus.READ;
    }

    public void resolve() {
        this.status = AlertStatus.RESOLVED;
    }

    public Long getId() {
        return id;
    }

    public AlertLevel getLevel() {
        return level;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public Long getViolationCount() {
        return violationCount;
    }

    public String getMessage() {
        return message;
    }
}