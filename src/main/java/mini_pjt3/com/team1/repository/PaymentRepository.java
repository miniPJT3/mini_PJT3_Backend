package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.Payment;
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

    List<Payment> findAllByStatusAndCreatedAtBefore(TransactionStatus status, LocalDateTime dateTime);

    List<Payment> findAllByProduct_SellerId(Long sellerId);

    long countByStatusAndCreatedAtAfter(TransactionStatus status, LocalDateTime createdAt);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            LocalDateTime start,
            LocalDateTime end
    );

    long countByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            TransactionStatus status,
            LocalDateTime start,
            LocalDateTime end
    );
}