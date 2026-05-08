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
     * [Service B] 가상 계좌 입금 승인 Webhook
     * 입금 시뮬레이터(FE)에서 이 API를 호출하면 결제가 완료됨.
     */
    @PostMapping("/deposit")
    public ResponseEntity<PaymentResponse> approveDeposit(@RequestBody @Valid PaymentRequest request) {
        PaymentResponse response = paymentService.processDeposit(request);

        // 성공 응답 반환
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
}