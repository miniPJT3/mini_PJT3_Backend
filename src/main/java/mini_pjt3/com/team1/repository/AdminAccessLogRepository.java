package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.AdminAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminAccessLogRepository extends JpaRepository<AdminAccessLog, Long> {

    List<AdminAccessLog> findTop50ByOrderByCreatedAtDesc();

    long countByCreatedAtAfter(LocalDateTime createdAt);
}