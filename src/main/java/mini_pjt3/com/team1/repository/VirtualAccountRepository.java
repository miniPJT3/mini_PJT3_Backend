package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.VirtualAccount;
import mini_pjt3.com.team1.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Long> {

    @Query("""
            SELECT va
            FROM VirtualAccount va
            JOIN FETCH va.payment p
            WHERE p.status = :status
            """)
    List<VirtualAccount> findAllByPaymentStatusForAudit(@Param("status") TransactionStatus status);
}