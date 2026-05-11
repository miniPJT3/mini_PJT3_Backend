package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import mini_pjt3.com.team1.enums.TransactionStatus;

public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Long> {

    /**
     * 결제 ID(payment_id)로 가상 계좌를 조회합니다.
     * 엔티티에 Payment 객체로 매핑되어 있다면
     * JPA가 findByPaymentId(Long paymentId)를 해석해서 쿼리를 날려줍니다.
     */
    Optional<VirtualAccount> findByPaymentId(Long paymentId);

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
}