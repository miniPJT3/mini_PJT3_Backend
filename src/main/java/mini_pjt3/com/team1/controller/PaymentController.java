package mini_pjt3.com.team1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.request.PaymentRequest;
import mini_pjt3.com.team1.dto.response.PaymentResponse;
import mini_pjt3.com.team1.service.PaymentService;
import org.springframework.http.ResponseEntity;
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
            @RequestBody PaymentRequest request) {

        // 현재는 테스트를 위해 memberId를 1L로 고정하여 진행합니다.
        // 추후 인증 로직(세션 등)이 추가되면 변경 가능합니다.
        Long memberId = 1L;

        PaymentResponse response = paymentService.issueVirtualAccount(memberId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 결제 내역 조회 API
     */
    @GetMapping("/history")
    public ResponseEntity<List<PaymentResponse>> getMyPaymentHistory() {
        // 1. 실제 운영 환경에서는 아래처럼 시큐리티 컨텍스트에서 유저 정보를 가져와야 합니다.
        // Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 2. 지금은 테스트 단계이므로 데이터가 들어간 1번 멤버의 내역을 가져오도록 고정합니다.
        Long testMemberId = 1L;

        List<PaymentResponse> history = paymentService.getMyHistory(testMemberId);

        // 만약 내역이 비어있으면 204 No Content를 줄 수도 있지만,
        // 빈 리스트([])를 주는 것이 프론트에서 처리하기 더 편합니다.
        return ResponseEntity.ok(history);
    }

    // 🥊 판매자 대시보드용 목록 조회 (sellerId는 현재 1로 고정해서 테스트)
    @GetMapping("/seller/{sellerId}/history")
    public ResponseEntity<List<PaymentResponse>> getPendingPayments(@PathVariable Long sellerId) {
        System.out.println("조회 요청된 판매자 ID: " + sellerId);
        List<PaymentResponse> responses = paymentService.getPaymentsBySeller(sellerId);
        return ResponseEntity.ok(responses);
    }

    // 🥊 판매자의 최종 입금 확인 승인 버튼 클릭 시 호출
    @PostMapping("/approve/{payUuid}")
    public ResponseEntity<Void> approvePayment(@PathVariable String payUuid) {
        // 🥊 현재는 테스트를 위해 판매자 ID를 1로 고정하여 전달(상태 : PENDING -> PAID)
        paymentService.approvePayment(payUuid, 1L);
        return ResponseEntity.ok().build();
    }

    // 1. [구매자] "입금 완료" 버튼 클릭 시 호출
    @PostMapping("/report-deposit/{payUuid}")
    public ResponseEntity<PaymentResponse> reportDeposit(@PathVariable String payUuid) {
        // 구매자가 가상계좌로 입금 후 보고하는 단계 (상태: PENDING -> DEPOSITED)
        return ResponseEntity.ok(paymentService.reportDeposit(payUuid));
    }
}