package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.*;
import mini_pjt3.com.team1.enums.TransactionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Payment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String payUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL)
    private VirtualAccount virtualAccount;

    private LocalDateTime paidAt;

    @Builder
    public Payment(Long totalAmount, String productName, Member member, Product product, VirtualAccount virtualAccount, TransactionStatus status) {
        this.payUuid = UUID.randomUUID().toString(); // 생성 시 자동 생성
        this.productName = productName;
        this.totalAmount = totalAmount;
        this.member = member;
        this.product = product;
        // status가 null로 들어오면 기본값 PENDING 설정
        this.status = (status != null) ? status : TransactionStatus.PENDING;
        this.virtualAccount = virtualAccount;
    }

    /**
     * 결제 상태 변경 메서드
     */
    public void updateStatus(TransactionStatus status) {
        this.status = status;
    }

    @PrePersist
    public void createUuid() {
        if (this.payUuid == null) { // 이미 생성되어 있지 않을 때만 생성
            this.payUuid = UUID.randomUUID().toString();
        }
    }
}