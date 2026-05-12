package mini_pjt3.com.team1.dto.response;

import lombok.*;
import mini_pjt3.com.team1.entity.Payment; // Import Payment entity
import mini_pjt3.com.team1.enums.TransactionStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentResponse {

    private String payUuid;          // 결제 고유 식별자
    private TransactionStatus status;
    private Long depositedAmount;    // 실제 입금된 금액
    private String maskedAccount;    // 보안을 위해 마스킹 처리된 계좌번호
    private LocalDateTime paidAt;    // 최종 결제 완료 시간
    private Long totalAmount;
    private String productName;
    private String memberName;
    private java.time.LocalDateTime createdAt;
    private String bankName;
    private String message;          // 응답 메시지 (예: "결제가 성공적으로 완료되었습니다.")

    // 성공 응답 정적 팩토리 메서드
    public static PaymentResponse success(String payUuid, Long amount, String maskedAccount) {
        return PaymentResponse.builder()
                .payUuid(payUuid)
                .status(TransactionStatus.PAID) // Use TransactionStatus.PAID
                .depositedAmount(amount)
                .maskedAccount(maskedAccount)
                .paidAt(LocalDateTime.now())
                .message("입금 확인 및 결제 처리가 완료되었습니다.")
                .build();
    }

    // Payment 엔티티로부터 PaymentResponse를 생성하는 팩토리 메서드
    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .payUuid(payment.getPayUuid())
                .status(payment.getStatus())
                .depositedAmount(payment.getTotalAmount()) // Assuming depositedAmount can be totalAmount initially
                .productName(payment.getProductName())
                .totalAmount(payment.getTotalAmount())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}