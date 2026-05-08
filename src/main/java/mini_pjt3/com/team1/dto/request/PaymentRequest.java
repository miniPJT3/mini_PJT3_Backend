package mini_pjt3.com.team1.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    private String payUuid;

    private String transactionId;

    @NotBlank(message = "상품명은 필수입니다.")
    private String productName; 

    @NotNull(message = "입금액은 필수입니다.")
    @Min(value = 100, message = "최소 결제 금액은 100원 이상입니다.")
    private Long depositedAmount;

}