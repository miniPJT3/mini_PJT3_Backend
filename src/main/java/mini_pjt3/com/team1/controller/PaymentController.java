package mini_pjt3.com.team1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.request.PaymentRequest;
import mini_pjt3.com.team1.dto.response.PaymentResponse;
import mini_pjt3.com.team1.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 가상계좌 발급 요청
     * POST /api/payments/issue
     */
    @PostMapping("/issue")
    public ResponseEntity<PaymentResponse> issueAccount(
            @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // extractIdFromPrincipal 대신 서비스에 '아이디'를 넘겨서 처리
        String loginId = userDetails.getUsername();
        PaymentResponse response = paymentService.issueByLoginId(loginId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * 내 결제 내역 조회 API (구매자용)
     */
    @GetMapping("/history")
    public ResponseEntity<List<PaymentResponse>> getMyPaymentHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        // 세션에서 로그인 아이디 추출
        String loginId = userDetails.getUsername();

        // 서비스에 loginId를 넘겨서 본인의 내역만 가져오게 함
        List<PaymentResponse> history = paymentService.getMyHistoryByLoginId(loginId);

        return ResponseEntity.ok(history);
    }

    /**
     * 판매자 대시보드용 목록 조회 (판매자 ID: 10 고정)
     */
    @GetMapping("/seller/history") // 경로에서 {sellerId} 제거
    public ResponseEntity<List<PaymentResponse>> getPendingPayments() {
        Long fixedSellerId = 10L;
        System.out.println("판매자 대시보드 조회 요청 (ID: 10)");

        List<PaymentResponse> responses = paymentService.getPaymentsBySeller(fixedSellerId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 판매자의 최종 입금 확인 승인 (판매자 ID: 10 고정)
     */
    @PostMapping("/approve/{payUuid}")
    public ResponseEntity<Void> approvePayment(@PathVariable String payUuid) {
        // 판매자 ID를 10으로 고정하여 전달
        paymentService.approvePayment(payUuid, 10L);
        return ResponseEntity.ok().build();
    }

    /**
     * [구매자] "입금 완료" 보고
     */
    @PostMapping("/report-deposit/{payUuid}")
    public ResponseEntity<PaymentResponse> reportDeposit(@PathVariable String payUuid) {
        return ResponseEntity.ok(paymentService.reportDeposit(payUuid));
    }
}