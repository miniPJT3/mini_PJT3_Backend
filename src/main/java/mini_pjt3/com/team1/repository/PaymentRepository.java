package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.Payment;
import mini_pjt3.com.team1.enums.PaymentStatus;
import mini_pjt3.com.team1.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPayUuid(String payUuid);

    List<Payment> findAllByMember(Member member);

    List<Payment> findByStatusAndCreatedAtBetween(
            PaymentStatus status,
            LocalDateTime start,
            LocalDateTime end
    );
}