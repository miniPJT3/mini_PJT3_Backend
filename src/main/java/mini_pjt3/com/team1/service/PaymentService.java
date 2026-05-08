package mini_pjt3.com.team1.service;

import mini_pjt3.com.team1.dto.request.PaymentRequest;
import mini_pjt3.com.team1.dto.response.PaymentResponse;

public interface PaymentService {

    /**
     * [Service B] 입금 승인 및 검증 처리
     * @param request 입금 시뮬레이터에서 넘어온 데이터 (UUID, 거래ID, 입금액 등)
     * @return 결제 완료 정보 및 마스킹된 계좌 정보
     */
    PaymentResponse processDeposit(PaymentRequest request);
}