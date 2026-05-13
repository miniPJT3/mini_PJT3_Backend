package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mini_pjt3.com.team1.enums.AuditResult;

@Entity
@Getter
@Table(
        name = "masking_audit_logs",
        indexes = {
                @Index(name = "idx_masking_audit_payment_id", columnList = "payment_id"),
                @Index(name = "idx_masking_audit_result", columnList = "result"),
                // BaseEntity의 createdAt 필드가 DB의 created_at 컬럼과 매핑되므로 아래 설정은 올바릅니다.
                @Index(name = "idx_masking_audit_created_at", columnList = "created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    // 내부 객체 생성을 위한 생성자
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

    /**
     * 마스킹 성공 로그 생성
     */
    public static MaskingAuditLog success(Long paymentId, Long virtualAccountId, String maskedAccountNumber) {
        return new MaskingAuditLog(
                paymentId,
                virtualAccountId,
                maskedAccountNumber,
                AuditResult.SUCCESS,
                "정상 마스킹 처리되었습니다."
        );
    }

    /**
     * 마스킹 실패 로그 생성
     */
    public static MaskingAuditLog fail(Long paymentId, Long virtualAccountId, String maskedAccountNumber, String reason) {
        return new MaskingAuditLog(
                paymentId,
                virtualAccountId,
                maskedAccountNumber,
                AuditResult.FAIL,
                reason
        );
    }
}