package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentHistory extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", unique = true)
    private Payment payment; // Payment와 1:1

    @Column(unique = true, nullable = false)
    private String transactionId; // 은행 측 고유 거래 ID (중복 결제 방지용)

    @Column(nullable = false)
    private Long depositedAmount; // 실제 입금 금액

    @Column(nullable = false)
    private LocalDateTime paidAt; // 입금 확정 시각

    @Builder
    public PaymentHistory(Payment payment, String transactionId, Long depositedAmount, LocalDateTime paidAt) {
        this.payment = payment;
        this.transactionId = transactionId;
        this.depositedAmount = depositedAmount;
        this.paidAt = paidAt;
    }
}