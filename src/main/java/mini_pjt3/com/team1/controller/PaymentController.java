package mini_pjt3.com.team1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.request.PaymentRequest;
import mini_pjt3.com.team1.dto.response.PaymentResponse;
import mini_pjt3.com.team1.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 가상계좌 발급 요청
     */
    @PostMapping("/issue")
    public ResponseEntity<?> issueAccount(
            @RequestBody PaymentRequest request,
            @AuthenticationPrincipal String email // 🥊 String 유지
    ) {
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }

        PaymentResponse response = paymentService.issueByEmail(email, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 결제 내역 조회 API (구매자용)
     */
    // PaymentController.java

    @GetMapping("/history")
    public ResponseEntity<?> getMyPaymentHistory(@AuthenticationPrincipal String email) {
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }

        // 🥊 바뀐 메서드명으로 호출!
        List<PaymentResponse> history = paymentService.getMyHistoryByEmail(email);

        return ResponseEntity.ok(history);
    }

    /**
     * 판매자 대시보드용 목록 조회 (판매자 ID: 10 고정)
     */
    @GetMapping("/seller/history")
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

    @PostMapping("/{payUuid}/expire")
    public ResponseEntity<Void> expirePayment(@PathVariable String payUuid) {
        // 🥊 여기서 ServiceImpl의 expirePayment를 호출합니다!
        paymentService.expirePayment(payUuid);

        // 성공 시 200 OK 응답을 보냅니다.
        return ResponseEntity.ok().build();
    }
}