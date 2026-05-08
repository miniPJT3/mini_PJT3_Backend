package mini_pjt3.com.team1.service.impl;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.request.PaymentRequest;
import mini_pjt3.com.team1.dto.response.PaymentResponse;
import mini_pjt3.com.team1.entity.Payment;
import mini_pjt3.com.team1.entity.PaymentHistory;
import mini_pjt3.com.team1.entity.VirtualAccount;
import mini_pjt3.com.team1.enums.TransactionStatus;
import mini_pjt3.com.team1.repository.PaymentHistoryRepository;
import mini_pjt3.com.team1.repository.PaymentRepository;
import mini_pjt3.com.team1.repository.VirtualAccountRepository;
import mini_pjt3.com.team1.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final VirtualAccountRepository virtualAccountRepository;

    @Override
    public PaymentResponse processDeposit(PaymentRequest request) {
        // 1. 멱등성 체크 (DB 레벨 Unique Index와 함께 이중 방어)
        if (paymentHistoryRepository.existsByTransactionId(request.getTransactionId())) {
            return null;
        }

        // 2. Payment 조회 (연관된 VirtualAccount까지 함께 고려)
        Payment payment = paymentRepository.findByPayUuid(request.getPayUuid())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제 요청입니다."));

        // 3. 상태 검증
        if (payment.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("결제 대기 상태가 아닙니다. 현재 상태: " + payment.getStatus());
        }

        // 4. 금액 검증
        if (!payment.getTotalAmount().equals(request.getDepositedAmount())) {
            // 정합성이 깨진 경우 로그를 남기거나 알림을 쏘는 로직이 필요할 수 있습니다.
            throw new IllegalArgumentException("입금액이 일치하지 않습니다. 요청액: " + payment.getTotalAmount());
        }

        // 5. 가상 계좌 상태 변경 및 마스킹 데이터 가져오기
        VirtualAccount va = virtualAccountRepository.findByPaymentId(payment.getId())
                .orElseThrow(() -> new IllegalStateException("연결된 가상 계좌를 찾을 수 없습니다."));

        va.completePayment(); // 여기서 status=USED 및 마스킹 로직 실행

        // 6. 상태 업데이트 및 이력 저장
        payment.updateStatus(TransactionStatus.PAID);

        PaymentHistory history = PaymentHistory.builder()
                .payment(payment)
                .transactionId(request.getTransactionId())
                .depositedAmount(request.getDepositedAmount())
                .paidAt(LocalDateTime.now())
                .build();
        paymentHistoryRepository.save(history);

        // 7. 최종 응답 DTO 생성 및 반환
        return PaymentResponse.success(
                payment.getPayUuid(),
                history.getDepositedAmount(),
                va.getMaskedAccountNumber()
        );
    }
}