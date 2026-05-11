package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.VirtualAccount;
import mini_pjt3.com.team1.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Long> {

    Optional<VirtualAccount> findByAccountNumber(String accountNumber);

    List<VirtualAccount> findAllByStatusAndExpiredAtBefore(AccountStatus status, LocalDateTime expiredAt);

    // @Modifying
    // @Query("UPDATE VirtualAccount v SET v.deletedAt = CURRENT_TIMESTAMP WHERE v.orderId = :orderId")
    // void softDeleteByOrderId(@Param("orderId") Long orderId);
}