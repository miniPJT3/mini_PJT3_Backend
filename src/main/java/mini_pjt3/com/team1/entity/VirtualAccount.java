package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.*;
import mini_pjt3.com.team1.enums.AccountStatus;
import mini_pjt3.com.team1.enums.BankCode;
import java.time.LocalDateTime;

@Entity
@Getter
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
}