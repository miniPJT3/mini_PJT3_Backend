package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.*;
import mini_pjt3.com.team1.enums.TransactionStatus;

import java.util.SimpleTimeZone;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String payUuid; // 외부 노출용 고유값

    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false)
    private String productName;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public Payment(Long totalAmount, String productName, Member member) {
        this.payUuid = UUID.randomUUID().toString();
        this.productName = productName;
        this.totalAmount = totalAmount;
        this.member = member;
        this.status = TransactionStatus.PENDING;
    }

    /**
     * 결제 상태 변경 메서드
     */
    public void updateStatus(TransactionStatus status) {
        this.status = status;
    }
}