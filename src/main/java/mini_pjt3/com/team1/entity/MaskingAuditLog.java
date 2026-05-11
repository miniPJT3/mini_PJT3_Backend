package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import mini_pjt3.com.team1.enums.AuditResult;

@Entity
@Table(
        name = "masking_audit_logs",
        indexes = {
                @Index(name = "idx_masking_audit_payment_id", columnList = "payment_id"),
                @Index(name = "idx_masking_audit_result", columnList = "result"),
                @Index(name = "idx_masking_audit_created_at", columnList = "created_at")
        }
)
public class MaskingAuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "virtual_account_id", nullable = false)
    private Long virtualAccountId;

    @Column(name = "masked_account_number")
    private String maskedAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditResult result;

    @Column(columnDefinition = "TEXT")
    private String reason;

    protected MaskingAuditLog() {
    }

    private MaskingAuditLog(
            Long paymentId,
            Long virtualAccountId,
            String maskedAccountNumber,
            AuditResult result,
            String reason
    ) {
        this.paymentId = paymentId;
        this.virtualAccountId = virtualAccountId;
        this.maskedAccountNumber = maskedAccountNumber;
        this.result = result;
        this.reason = reason;
    }

    public static MaskingAuditLog success(Long paymentId, Long virtualAccountId, String maskedAccountNumber) {
        return new MaskingAuditLog(
                paymentId,
                virtualAccountId,
                maskedAccountNumber,
                AuditResult.SUCCESS,
                "마스킹 값이 정상적으로 존재합니다."
        );
    }

    public static MaskingAuditLog fail(Long paymentId, Long virtualAccountId, String maskedAccountNumber, String reason) {
        return new MaskingAuditLog(
                paymentId,
                virtualAccountId,
                maskedAccountNumber,
                AuditResult.FAIL,
                reason
        );
    }

    public Long getId() {
        return id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getVirtualAccountId() {
        return virtualAccountId;
    }

    public String getMaskedAccountNumber() {
        return maskedAccountNumber;
    }

    public AuditResult getResult() {
        return result;
    }

    public String getReason() {
        return reason;
    }
}