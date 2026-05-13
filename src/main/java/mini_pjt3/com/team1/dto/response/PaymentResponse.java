package mini_pjt3.com.team1.dto.response;

import lombok.*;
import mini_pjt3.com.team1.entity.Payment;
import mini_pjt3.com.team1.entity.VirtualAccount;
import mini_pjt3.com.team1.enums.TransactionStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentResponse {

    private String payUuid;          // 결제 고유 식별자
    private TransactionStatus status; // 결제 상태 (PENDING, PAID, DEPOSITED 등)
    private Long depositedAmount;    // 실제 입금된 금액 (또는 총 금액)
    private Long totalAmount;        // 주문 총 금액
    private String productName;      // 상품명
    private String memberName;       // 구매자 이름
    private String bankName;         // 가상계좌 은행명
    private String maskedAccount;    // 마스킹 처리된 계좌번호
    private String message;          // 응답 메시지
    private LocalDateTime paidAt;    // 최종 결제 완료 시간
    private LocalDateTime createdAt; // 결제 생성 시간
    private LocalDateTime expiredAt; // 가상계좌 입금 만료 시간

    /**
     * 🥊 [팩토리 메서드] Payment 엔티티를 DTO로 변환
     * 서비스 레이어에서 .map(PaymentResponse::from)으로 한 번에 변환할 때 사용합니다.
     */
    public static PaymentResponse from(Payment payment) {
        VirtualAccount va = payment.getVirtualAccount();

        return PaymentResponse.builder()
                .payUuid(payment.getPayUuid())
                .status(payment.getStatus())
                .productName(payment.getProductName())
                .totalAmount(payment.getTotalAmount())
                // 🥊 프론트에서 금액을 찾을 때 depositedAmount도 함께 보게 함
                .depositedAmount(payment.getTotalAmount())
                .bankName(va != null ? va.getBankName() : "계좌 정보 없음")
                .maskedAccount(va != null ? va.getMaskedAccountNumber() : "계좌 정보 없음")
                .memberName(payment.getMember() != null ? payment.getMember().getName() : "알 수 없음")
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .expiredAt(va != null ? va.getExpiredAt() : null)
                .message("데이터 조회가 완료되었습니다.")
                .build();
    }

    /**
     * 🥊 [팩토리 메서드] 결제 성공 시 응답용
     */
    public static PaymentResponse success(String payUuid, Long amount, String maskedAccount) {
        return PaymentResponse.builder()
                .payUuid(payUuid)
                .status(TransactionStatus.PAID)
                .depositedAmount(amount)
                .maskedAccount(maskedAccount)
                .paidAt(LocalDateTime.now())
                .message("입금 확인 및 결제 처리가 완료되었습니다.")
                .build();
    }
}