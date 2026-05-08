package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Long> {

    /**
     * 결제 ID(payment_id)로 가상 계좌를 조회합니다.
     * 엔티티에 Payment 객체로 매핑되어 있다면
     * JPA가 findByPaymentId(Long paymentId)를 해석해서 쿼리를 날려줍니다.
     */
    Optional<VirtualAccount> findByPaymentId(Long paymentId);
}
