package mini_pjt3.com.team1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.request.PaymentRequest;
import mini_pjt3.com.team1.dto.response.ApiResponse;
import mini_pjt3.com.team1.dto.response.PaymentResponse;
import mini_pjt3.com.team1.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication
    ) {
        PaymentResponse response = paymentService.createPayment(request, authentication);

        return ResponseEntity.ok(
                ApiResponse.success("결제 생성 성공", response)
        );
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable Long paymentId
    ) {
        PaymentResponse response = paymentService.getPayment(paymentId);

        return ResponseEntity.ok(
                ApiResponse.success("결제 조회 성공", response)
        );
    }

    @GetMapping("/{paymentId}/status")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentStatus(
            @PathVariable Long paymentId
    ) {
        PaymentResponse response = paymentService.getPaymentStatus(paymentId);

        return ResponseEntity.ok(
                ApiResponse.success("결제 상태 조회 성공", response)
        );
    }
}