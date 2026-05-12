package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.*;
import mini_pjt3.com.team1.enums.AccountStatus;
import mini_pjt3.com.team1.enums.BankCode;
import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor // Add AllArgsConstructor to work with Builder and existing constructor logic
@Builder // Add Builder to the class level
public class VirtualAccount extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String accountNumber;

    private String maskedAccountNumber;

    @Enumerated(EnumType.STRING)
    private BankCode bankCode;

    @Column(nullable = false)
    private String bankName;

    @Enumerated(EnumType.STRING)
    @Builder.Default // Default status for builder
    private AccountStatus status = AccountStatus.ACTIVE;

    @Builder.Default // Default expiredAt for builder
    private LocalDateTime expiredAt = LocalDateTime.now().plusHours(3);

    @Builder.Default // Default isDeleted for builder
    private boolean isDeleted = false; // Soft Delete

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // Use the @Builder generated constructor. If a custom constructor is needed for other purposes, keep it separate.
    // The existing constructor logic should be handled by @Builder and @Builder.Default fields.
    // If this constructor is still needed for non-builder creation, it should be complete.
    // For now, rely on the @Builder for flexibility. The existing constructor was partial.
    // Removing the custom constructor as @Builder will handle it, and the custom one was incomplete.

    /**
     * [Service B] 입금 완료 시 호출되는 비즈니스 로직
     */
    public void completePayment() {
        // 1. 상태 변경
        this.status = AccountStatus.USED;

        // 2. 마스킹 로직 (앞 4자리-뒤 4자리만 남기기)
        if (this.accountNumber != null && this.accountNumber.length() > 8) {
            this.maskedAccountNumber = this.accountNumber.substring(0, 4)
                    + "****"
                    + this.accountNumber.substring(this.accountNumber.length() - 4);
        } else {
            this.maskedAccountNumber = "****"; // 예외 케이스
        }

        // 3. Soft Delete 처리
        this.isDeleted = true;
    }
}