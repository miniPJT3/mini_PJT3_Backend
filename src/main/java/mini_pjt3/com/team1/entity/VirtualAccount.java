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

    @Enumerated(EnumType.STRING)
    private BankCode bankCode;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    private LocalDateTime expiredAt;
    private boolean isDeleted = false; // Soft Delete

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public VirtualAccount(String accountNumber, BankCode bankCode, Payment payment, Member member) {
        this.accountNumber = accountNumber;
        this.bankCode = bankCode;
        this.payment = payment;
        this.member = member;
        this.status = AccountStatus.ACTIVE;
        this.expiredAt = LocalDateTime.now().plusHours(3);
    }

    public void expire() {
        this.status = AccountStatus.EXPIRED;
        this.isDeleted = true;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}