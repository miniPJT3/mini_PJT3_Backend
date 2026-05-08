package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    /**
     * 멱등성 검증을 위한 필수 메서드
     * 은행에서 보내준 고유 거래 ID(transactionId)가 이미 DB에 존재하는지 확인합니다.
     */
    boolean existsByTransactionId(String transactionId);

    /**
     * 특정 결제 요청(Payment)에 대한 입금 이력을 조회합니다.
     */
    Optional<PaymentHistory> findByPaymentId(Long paymentId);
}