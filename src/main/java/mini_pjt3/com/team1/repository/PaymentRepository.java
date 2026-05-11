package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.Payment;
import mini_pjt3.com.team1.entity.VirtualAccount;
import mini_pjt3.com.team1.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPayUuid(String payUuid);

    @Query("SELECT p FROM Payment p WHERE p.member.id = :memberId ORDER BY p.createdAt DESC")
    List<Payment> findAllByMemberId(@Param("memberId") Long memberId);

    // 상태가 PENDING이고, 특정 시간(3시간 전) 이전에 생성된 데이터 찾기
    List<Payment> findAllByStatusAndCreatedAtBefore(String status, LocalDateTime dateTime);

    List<Payment> findAllByProduct_SellerId(Long sellerId);

}
