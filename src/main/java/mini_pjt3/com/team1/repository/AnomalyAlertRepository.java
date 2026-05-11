package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.AnomalyAlert;
import mini_pjt3.com.team1.enums.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AnomalyAlertRepository extends JpaRepository<AnomalyAlert, Long> {

    List<AnomalyAlert> findTop50ByOrderByCreatedAtDesc();

    long countByStatus(AlertStatus status);

    boolean existsBySourceIpAndTitleAndStatusAndCreatedAtAfter(
            String sourceIp,
            String title,
            AlertStatus status,
            LocalDateTime createdAt
    );

    boolean existsByTitleAndStatusAndCreatedAtAfter(
            String title,
            AlertStatus status,
            LocalDateTime createdAt
    );
}