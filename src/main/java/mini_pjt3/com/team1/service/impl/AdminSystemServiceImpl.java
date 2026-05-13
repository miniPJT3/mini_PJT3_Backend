package mini_pjt3.com.team1.service.impl;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.response.AdminSystemStatusResponse;
import mini_pjt3.com.team1.enums.AccountStatus;
import mini_pjt3.com.team1.enums.TransactionStatus;
import mini_pjt3.com.team1.repository.PaymentRepository;
import mini_pjt3.com.team1.repository.VirtualAccountRepository;
import mini_pjt3.com.team1.service.AdminSystemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSystemServiceImpl implements AdminSystemService {

    private final VirtualAccountRepository virtualAccountRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public AdminSystemStatusResponse getSystemStatus() {
        // 1. 활성 가상계좌 수
        long activeVirtualAccountCount = virtualAccountRepository.countByStatus(AccountStatus.ACTIVE);

        // 2. 한국 시간 기준 오늘 범위
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

        // 3. 오늘 주문 건수 집계
        long total = paymentRepository.countByCreatedAtBetween(startOfToday, endOfToday);
        long paid = paymentRepository.countByStatusAndCreatedAtBetween(TransactionStatus.PAID, startOfToday, endOfToday);
        long failed = paymentRepository.countByStatusAndCreatedAtBetween(TransactionStatus.FAILED, startOfToday, endOfToday);

        // 4. 성공률 계산
        double successRate = (total == 0) ? 0.0 :
                Math.round((paid * 100.0 / total) * 10) / 10.0;

        return new AdminSystemStatusResponse(
                activeVirtualAccountCount,
                total,
                paid,
                failed,
                successRate
        );
    }
}