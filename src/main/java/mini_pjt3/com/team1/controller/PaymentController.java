package mini_pjt3.com.team1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.request.PaymentRequest;
import mini_pjt3.com.team1.dto.response.PaymentResponse;
import mini_pjt3.com.team1.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * [Service B] 가상 계좌 입금 승인 Webhook
     * 입금 시뮬레이터(FE)에서 이 API를 호출하면 결제가 완료됨.
     */
    @PostMapping("/deposit")
    public ResponseEntity<PaymentResponse> approveDeposit(@RequestBody @Valid PaymentRequest request) {
        // 지호 네가 만든 ServiceImpl의 로직이 여기서 실행됨!
        PaymentResponse response = paymentService.processDeposit(request);

        // 성공 응답 반환
        return ResponseEntity.ok(response);
    }
}