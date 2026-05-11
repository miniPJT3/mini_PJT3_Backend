package mini_pjt3.com.team1.service;

import mini_pjt3.com.team1.dto.request.PaymentRequest;
import mini_pjt3.com.team1.dto.request.SimulationRequest;
import mini_pjt3.com.team1.dto.response.PaymentResponse;
import mini_pjt3.com.team1.dto.response.SimulationResponse;

import java.util.List;

public interface PaymentService {

    // 판매자 ID별 판매 목록 조회
    List<PaymentResponse> getPaymentsBySeller(Long sellerId);

    // 판매자의 입금 확인 승인 처리 (상태 변경: DEPOSITED -> COMPLETED)
    void approvePayment(String payUuid, Long sellerId);

    // 내역 조회를 위한 loginId 기반 메서드 추가
    List<PaymentResponse> getMyHistoryByLoginId(String loginId);

    // 3. 구매자 입금 보고 로직
    PaymentResponse reportDeposit(String payUuid);

    // 가상계좌 발급 메서드 선언
    PaymentResponse issueByLoginId(String loginId, PaymentRequest request);

    SimulationResponse checkSimulationLogic(SimulationRequest request);
}