package mini_pjt3.com.team1.controller;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.response.ApiResponse;
import mini_pjt3.com.team1.dto.response.PaymentResponse;
import mini_pjt3.com.team1.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-history")
@RequiredArgsConstructor
public class PaymentHistoryController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentHistory() {

        List<PaymentResponse> response = paymentService.getPaymentHistories();

        return ResponseEntity.ok(
                ApiResponse.success("결제 이력 조회 성공", response)
        );
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentHistoryDetail(
            @PathVariable Long paymentId
    ) {
        PaymentResponse response = paymentService.getPayment(paymentId);

        return ResponseEntity.ok(
                ApiResponse.success("결제 상세 조회 성공", response)
        );
    }
}