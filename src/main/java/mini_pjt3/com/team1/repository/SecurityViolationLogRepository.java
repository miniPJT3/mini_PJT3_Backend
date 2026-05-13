package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.SecurityViolationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SecurityViolationLogRepository extends JpaRepository<SecurityViolationLog, Long> {
    
    List<SecurityViolationLog> findAllByOrderByCreatedAtDesc();    

    /**
     * 실시간 보안 관제 UI용: 최신 로그 10개 조회
     */
    List<SecurityViolationLog> findTop10ByOrderByCreatedAtDesc();

    /**
     * 프론트엔드 AdminDashboard에서 요청하는 최신 로그 20건 조회
     */
    List<SecurityViolationLog> findTop20ByOrderByCreatedAtDesc();

    /**
     * 전체 이력 조회용 (기존 Top50 유지)
     */
    List<SecurityViolationLog> findTop50ByOrderByCreatedAtDesc();

    /**
     * 특정 시간 이후 발생한 전체 위협 건수 (대시보드 카운터용)
     */
    long countByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * 특정 IP의 공격 횟수 카운트 (이상 징후 탐지용)
     */
    long countByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime createdAt);

    /**
     * 시간별 보안 위협 추이 분석 (차트 데이터용)
     */
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