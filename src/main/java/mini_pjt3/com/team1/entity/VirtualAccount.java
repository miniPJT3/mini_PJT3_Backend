package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.*;
import mini_pjt3.com.team1.enums.AccountStatus;
import mini_pjt3.com.team1.enums.BankCode;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private AccountStatus status;

    private LocalDateTime expiredAt;
    private boolean isDeleted = false; // Soft Delete

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Builder
    public VirtualAccount(String accountNumber, String bankName, BankCode bankCode, Payment payment) {
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.bankCode = bankCode;
        this.payment = payment;
        this.status = AccountStatus.ACTIVE;
        this.expiredAt = LocalDateTime.now().plusHours(3);
    }

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