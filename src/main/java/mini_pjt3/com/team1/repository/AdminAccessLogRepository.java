package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.AdminAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminAccessLogRepository extends JpaRepository<AdminAccessLog, Long> {

    List<AdminAccessLog> findTop20ByOrderByCreatedAtDesc();

    long countByCreatedAtAfter(LocalDateTime createdAt);

    @Modifying
    @Transactional
    void deleteByCreatedAtBefore(LocalDateTime dateTime);
}