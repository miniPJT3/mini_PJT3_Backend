package mini_pjt3.com.team1.service;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.entity.Payment;
import mini_pjt3.com.team1.enums.AccountStatus;
import mini_pjt3.com.team1.enums.TransactionStatus;
import mini_pjt3.com.team1.repository.PaymentRepository;
import mini_pjt3.com.team1.repository.VirtualAccountRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VaExpireScheduler {

    private final PaymentRepository paymentRepository;
    private final VirtualAccountRepository virtualAccountRepository; // 계좌 상태도 바꿔야 하니까 추가!

    // 10분마다 실행해서 촘촘하게 감시 (0초, 10분, 20분...)
    @Scheduled(cron = "0 0/10 * * * *")
    @Transactional
    public void expirePayments() {
        // 기준 시간: 현재로부터 3시간 전
        LocalDateTime limitTime = LocalDateTime.now().minusHours(3);

        // PENDING 또는 DEPOSITED 상태인 것들 중 updatedAt이 기준 시간 이전인 것 조회
        // (JPA Repository에 해당 메서드를 만들어야 합니다)
        List<Payment> expiredPayments = paymentRepository.findAllByStatusInAndUpdatedAtBefore(
                List.of(TransactionStatus.PENDING, TransactionStatus.DEPOSITED),
                limitTime
        );

        expiredPayments.forEach(payment -> {
            // 결제 상태 만료 처리
            payment.updateStatus(TransactionStatus.EXPIRED);

            // 연결된 가상계좌도 같이 만료 처리
            virtualAccountRepository.findByPaymentId(payment.getId()).ifPresent(va -> {
                va.setStatus(AccountStatus.EXPIRED);
                System.out.println("가상계좌 만료 완료: " + va.getAccountNumber());
            });

            System.out.println("결제 만료 처리 완료 UUID: " + payment.getPayUuid());
        });
    }
}