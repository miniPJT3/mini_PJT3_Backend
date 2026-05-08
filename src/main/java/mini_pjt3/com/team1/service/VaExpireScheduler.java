package mini_pjt3.com.team1.service;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.entity.Payment;
import mini_pjt3.com.team1.enums.TransactionStatus;
import mini_pjt3.com.team1.repository.PaymentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VaExpireScheduler {

    private final PaymentRepository paymentRepository;

    // 1시간마다 실행 (또는 10분마다)
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expirePayments() {
        LocalDateTime limitTime = LocalDateTime.now().minusHours(3);

        // 3시간 전보다 이전에 생성된 PENDING 상태의 결제들을 가져와서
        List<Payment> expiredPayments = paymentRepository.findAllByStatusAndCreatedAtBefore("PENDING", limitTime);

        // 전부 EXPIRED로 변경
        expiredPayments.forEach(payment -> {
            payment.setStatus(TransactionStatus.EXPIRED);
            System.out.println("만료 처리된 결제 UUID: " + payment.getPayUuid());
        });
    }
}
