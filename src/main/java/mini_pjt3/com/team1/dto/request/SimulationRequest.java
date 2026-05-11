package mini_pjt3.com.team1.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SimulationRequest {
    private String accountNumber; // 사용자가 입력한 계좌번호
    private Long amount;          // 사용자가 입력한 입금 금액
    private String email;         // 사용자가 입력한 이메일
}