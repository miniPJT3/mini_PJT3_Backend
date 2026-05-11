package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.VirtualAccount;
import mini_pjt3.com.team1.enums.AccountStatus;
import mini_pjt3.com.team1.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Long> {

    Optional<VirtualAccount> findByPaymentId(Long paymentId);

    @Query("""
            SELECT COUNT(va)
            FROM VirtualAccount va
            WHERE va.status = :status
              AND va.isDeleted = false
            """)
    long countActiveVirtualAccounts(@Param("status") AccountStatus status);

    @Query("""
            SELECT va
            FROM VirtualAccount va
            JOIN FETCH va.payment p
            WHERE p.status = :status
            """)
    List<VirtualAccount> findAllByPaymentStatusForAudit(@Param("status") TransactionStatus status);
}