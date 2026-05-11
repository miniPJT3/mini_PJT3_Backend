package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.Payment;
import mini_pjt3.com.team1.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    long countByStatusAndCreatedAtAfter(TransactionStatus status, LocalDateTime createdAt);
}