package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.VirtualAccount;
import mini_pjt3.com.team1.enums.AccountStatus;
import mini_pjt3.com.team1.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Long> {

    Optional<VirtualAccount> findByPaymentId(Long paymentId);

    @Query("""
            SELECT COUNT(va)
            FROM VirtualAccount va
            WHERE va.status = :status
              AND va.isDeleted = false
            """)
    long countActiveVirtualAccounts(@Param("status") AccountStatus status);

    // 계좌 번호로 가상계좌 엔티티를 찾고, 연관된 Payment까지 페치 조인으로 가져오면 성능에 좋습니다.
    @Query("select v from VirtualAccount v join fetch v.payment where v.accountNumber = :accountNumber")
    Optional<VirtualAccount> findByAccountNumberWithPayment(@Param("accountNumber") String accountNumber);

    @Query("""
            SELECT va
            FROM VirtualAccount va
            JOIN FETCH va.payment p
            WHERE p.status = :status
            """)
    List<VirtualAccount> findAllByPaymentStatusForAudit(@Param("status") TransactionStatus status);

    // 상태가 ACTIVE인 계좌 수 조회
    long countByStatus(AccountStatus status);
}