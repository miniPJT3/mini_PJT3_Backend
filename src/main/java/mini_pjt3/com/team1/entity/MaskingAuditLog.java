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

    @Column(name = "virtual_account_id") // 계좌 정보가 없을 수도 있으므로 nullable 허용 고려
    private Long virtualAccountId;

    // --- 추가된 마스킹 결과 필드들 🥊 ---
    @Column(name = "masked_name")
    private String maskedName;

    @Column(name = "masked_account_number")
    private String maskedAccountNumber;

    @Column(name = "masked_phone")
    private String maskedPhone;

    @Column(name = "masked_email")
    private String maskedEmail;
    // ---------------------------------

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditResult result;

    @Column(columnDefinition = "TEXT")
    private String reason;

    // 확장된 생성자
    private MaskingAuditLog(
            Long paymentId,
            Long virtualAccountId,
            String maskedName,
            String maskedAccountNumber,
            String maskedPhone,
            String maskedEmail,
            AuditResult result,
            String reason
    ) {
        this.paymentId = paymentId;
        this.virtualAccountId = virtualAccountId;
        this.maskedName = maskedName;
        this.maskedAccountNumber = maskedAccountNumber;
        this.maskedPhone = maskedPhone;
        this.maskedEmail = maskedEmail;
        this.result = result;
        this.reason = reason;
    }

    /**
     * 통합 마스킹 성공 로그 생성
     */
    public static MaskingAuditLog success(
            Long paymentId,
            Long virtualAccountId,
            String maskedName,
            String maskedAccountNumber,
            String maskedPhone,
            String maskedEmail) {
        return new MaskingAuditLog(
                paymentId,
                virtualAccountId,
                maskedName,
                maskedAccountNumber,
                maskedPhone,
                maskedEmail,
                AuditResult.SUCCESS,
                "모든 개인정보가 정책에 따라 정상 마스킹되었습니다."
        );
    }

    /**
     * 마스킹 실패 로그 생성
     */
    public static MaskingAuditLog fail(Long paymentId, Long virtualAccountId, String maskedAccountNumber, String reason) {
        return new MaskingAuditLog(
                paymentId,
                virtualAccountId,
                null,                // maskedName
                maskedAccountNumber, // 실패한 시점의 계좌번호 기록
                null,                // maskedPhone
                null,                // maskedEmail
                AuditResult.FAIL,
                reason
        );
    }
}