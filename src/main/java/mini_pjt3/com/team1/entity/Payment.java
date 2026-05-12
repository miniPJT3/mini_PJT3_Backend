package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.*;
import mini_pjt3.com.team1.enums.TransactionStatus;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Builder.Default
    private String payUuid = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false)
    private String productName;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "masked_account")
    private String maskedAccount;

    private java.time.LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Setter
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(mappedBy = "payment", fetch = FetchType.LAZY)
    private VirtualAccount virtualAccount;

    // 결제 상태 변경 메서드
    public void updateStatus(TransactionStatus status) {
        this.status = status;
    }

    public void updateVirtualAccountInfo(
        String bankName,
        String maskedAccount
    ) {
        this.bankName = bankName;
        this.maskedAccount = maskedAccount;
    }
}