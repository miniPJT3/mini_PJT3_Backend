package mini_pjt3.com.team1.service;

import mini_pjt3.com.team1.dto.request.PaymentRequest;
import mini_pjt3.com.team1.dto.response.PaymentResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(
            PaymentRequest request,
            Authentication authentication
    );

    PaymentResponse getPayment(Long paymentId);

    PaymentResponse getPaymentStatus(Long paymentId);

    void confirmPayment(Long paymentId);

    List<PaymentResponse> getMyPayments(Authentication authentication);

    List<PaymentResponse> getPaymentHistories();

    void cancelPayment(Long paymentId);

    // 판매자 ID별 판매 목록 조회
    List<PaymentResponse> getPaymentsBySeller(Long sellerId);

    // 판매자의 입금 확인 승인 처리 (상태 변경: DEPOSITED -> COMPLETED)
    void approvePayment(String payUuid, Long sellerId);

    // 3. 구매자 입금 보고 로직
    PaymentResponse reportDeposit(String payUuid);

    // 가상계좌 발급 메서드 선언
    PaymentResponse issueVirtualAccount(Long memberId, PaymentRequest dto);

    List<PaymentResponse> getMyHistory(Long memberId);
}