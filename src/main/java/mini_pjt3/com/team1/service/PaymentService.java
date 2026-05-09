package mini_pjt3.com.team1.service;

import mini_pjt3.com.team1.dto.request.PaymentRequest;
import mini_pjt3.com.team1.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    // 판매자 ID별 승인 대기(DEPOSITED) 목록 조회
    List<PaymentResponse> getPendingPaymentsBySeller(Long sellerId);

    // 판매자의 입금 확인 승인 처리 (상태 변경: DEPOSITED -> COMPLETED)
    void approvePayment(String payUuid, Long sellerId);

    // 3. 구매자 입금 보고 로직
    PaymentResponse reportDeposit(String payUuid);

    // 가상계좌 발급 메서드 선언
    PaymentResponse issueVirtualAccount(Long memberId, PaymentRequest dto);

    List<PaymentResponse> getMyHistory(Long memberId);
}