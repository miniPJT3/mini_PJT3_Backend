package mini_pjt3.com.team1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SimulationResponse {
    private String status;        // "SUCCESS" 또는 "FAIL"
    private String message;       // 상세 에러 메시지 또는 성공 메시지
    private String resultAction;  // 실제 로직이라면 수행했을 작업 (예: "PAID로 상태 변경 예정")
    private Long originalAmount;  // (선택) 실제 DB에 기록된 주문 금액
}