package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPayUuid(String payUuid);
}
