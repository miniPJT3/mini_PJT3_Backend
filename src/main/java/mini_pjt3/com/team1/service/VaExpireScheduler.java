package mini_pjt3.com.team1.service;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.entity.VirtualAccount;
import mini_pjt3.com.team1.enums.AccountStatus;
import mini_pjt3.com.team1.repository.VirtualAccountRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VaExpireScheduler {

    private final VirtualAccountRepository virtualAccountRepository;

    @Scheduled(cron = "0 */5 * * * *") // 5분마다 실행
    @Transactional
    public void expireVirtualAccounts() {
        List<VirtualAccount> list =
                virtualAccountRepository.findAllByStatusAndExpiredAtBefore(
                        AccountStatus.ACTIVE,
                        LocalDateTime.now()
                );

        for (VirtualAccount va : list) {
            va.expire();
        }
    }
}