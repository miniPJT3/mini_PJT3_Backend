package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.*;
import mini_pjt3.com.team1.enums.TransactionStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentHistory extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private Long finalAmount;
    private LocalDateTime depositedAt;

    @Builder
    public PaymentHistory(Payment payment, TransactionStatus status, Long finalAmount) {
        this.payment = payment;
        this.status = status;
        this.finalAmount = finalAmount;
        this.depositedAt = LocalDateTime.now();
    }
}