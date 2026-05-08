package mini_pjt3.com.team1.dto.response;

import lombok.Builder;
import lombok.Getter;
import mini_pjt3.com.team1.entity.Payment;

@Getter
@Builder
public class PaymentResponse {

    private Long paymentId;
    private String orderId; // Assuming payUuid acts as orderId for now
    private Long amount;
    private String status;
    private String virtualAccountNumber;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getPayUuid())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .virtualAccountNumber(null) // This needs to be set in service layer if applicable
                .build();
    }
}