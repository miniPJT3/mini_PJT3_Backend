package mini_pjt3.com.team1.service;

import mini_pjt3.com.team1.dto.response.AdminSystemStatusResponse;
import mini_pjt3.com.team1.enums.AccountStatus;
import mini_pjt3.com.team1.enums.TransactionStatus;
import mini_pjt3.com.team1.repository.PaymentRepository;
import mini_pjt3.com.team1.repository.VirtualAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Transactional(readOnly = true)
public class AdminSystemService {

    private final VirtualAccountRepository virtualAccountRepository;
    private final PaymentRepository paymentRepository;

    public AdminSystemService(
            VirtualAccountRepository virtualAccountRepository,
            PaymentRepository paymentRepository
    ) {
        this.virtualAccountRepository = virtualAccountRepository;
        this.paymentRepository = paymentRepository;
    }

    public AdminSystemStatusResponse getSystemStatus() {
        long activeVirtualAccountCount =
                virtualAccountRepository.countActiveVirtualAccounts(AccountStatus.ACTIVE);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        long todayTotalOrderCount =
                paymentRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        startOfToday,
                        startOfTomorrow
                );

        long todayPaidOrderCount =
                paymentRepository.countByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        TransactionStatus.PAID,
                        startOfToday,
                        startOfTomorrow
                );

        long todayFailedPaymentCount =
                paymentRepository.countByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        TransactionStatus.FAILED,
                        startOfToday,
                        startOfTomorrow
                );

        double todayPaymentSuccessRate = todayTotalOrderCount == 0
                ? 0.0
                : Math.round((todayPaidOrderCount * 1000.0) / todayTotalOrderCount) / 10.0;

        return new AdminSystemStatusResponse(
                activeVirtualAccountCount,
                todayTotalOrderCount,
                todayPaidOrderCount,
                todayFailedPaymentCount,
                todayPaymentSuccessRate
        );
    }
}