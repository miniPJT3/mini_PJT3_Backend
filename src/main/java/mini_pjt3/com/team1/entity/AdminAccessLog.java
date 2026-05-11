package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "admin_access_logs",
        indexes = {
                @Index(name = "idx_admin_access_ip", columnList = "ip_address"),
                @Index(name = "idx_admin_access_path", columnList = "request_path"),
                @Index(name = "idx_admin_access_created_at", columnList = "created_at")
        }
)
public class AdminAccessLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    @Column(name = "request_method", nullable = false, length = 20)
    private String requestMethod;

    @Column(name = "request_path", nullable = false, length = 500)
    private String requestPath;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    protected AdminAccessLog() {
    }

    private AdminAccessLog(
            String username,
            String ipAddress,
            String requestMethod,
            String requestPath,
            int statusCode,
            String userAgent
    ) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
        this.statusCode = statusCode;
        this.userAgent = userAgent;
    }

    public static AdminAccessLog of(
            String username,
            String ipAddress,
            String requestMethod,
            String requestPath,
            int statusCode,
            String userAgent
    ) {
        return new AdminAccessLog(username, ipAddress, requestMethod, requestPath, statusCode, userAgent);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getUserAgent() {
        return userAgent;
    }
}