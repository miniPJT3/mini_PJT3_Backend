package mini_pjt3.com.team1.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    @NotNull(message = "결제 금액은 필수입니다.")
    @Min(value = 100, message = "최소 결제 금액은 100원입니다.")
    private Long amount;

    @NotBlank(message = "은행 코드는 필수입니다.")
    private String bankCode;

    private Long virtualAccountId;

    public Long getVirtualAccountId() {
        return virtualAccountId;
    }
}