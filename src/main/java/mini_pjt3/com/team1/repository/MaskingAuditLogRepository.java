package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.MaskingAuditLog;
import mini_pjt3.com.team1.enums.AuditResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface MaskingAuditLogRepository extends JpaRepository<MaskingAuditLog, Long> {

    List<MaskingAuditLog> findTop50ByOrderByCreatedAtDesc();

    long countByResult(AuditResult result);

    @Modifying
    @Transactional
    void deleteByCreatedAtBefore(LocalDateTime dateTime);
}