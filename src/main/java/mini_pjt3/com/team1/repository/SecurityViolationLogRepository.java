package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.SecurityViolationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SecurityViolationLogRepository extends JpaRepository<SecurityViolationLog, Long> {

    List<SecurityViolationLog> findTop50ByOrderByCreatedAtDesc();

    long countByCreatedAtAfter(LocalDateTime createdAt);

    long countByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime createdAt);

    @Query(
            value = """
                    SELECT DATE_FORMAT(created_at, '%Y-%m-%d %H:00:00') AS hour_text,
                           COUNT(*) AS violation_count
                    FROM security_violation_logs
                    WHERE created_at >= :from
                    GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d %H:00:00')
                    ORDER BY hour_text
                    """,
            nativeQuery = true
    )
    List<Object[]> countHourlyViolations(@Param("from") LocalDateTime from);
}