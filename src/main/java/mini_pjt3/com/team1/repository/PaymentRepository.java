package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.Payment;
import mini_pjt3.com.team1.entity.VirtualAccount;
import mini_pjt3.com.team1.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPayUuid(String payUuid);

    @Query("SELECT p FROM Payment p WHERE p.member.id = :memberId ORDER BY p.createdAt DESC")
    List<Payment> findAllByMemberId(@Param("memberId") Long memberId);

    // 상태가 PENDING이고, 특정 시간(3시간 전) 이전에 생성된 데이터 찾기
    List<Payment> findAllByStatusAndCreatedAtBefore(String status, LocalDateTime dateTime);

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

    // 특정 기간(오늘) 내 전체 주문 수 조회
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 특정 기간 내 특정 상태(PAID, FAILED)인 주문 수 조회
    long countByStatusAndCreatedAtBetween(TransactionStatus status, LocalDateTime start, LocalDateTime end);

    @Query(value = """
        SELECT COALESCE(SUM(p.total_amount), 0)
        FROM payment p
        JOIN product pr ON p.product_id = pr.id
        WHERE pr.seller_id = :sellerId
          AND p.status = 'PAID'
    """, nativeQuery = true)
    BigDecimal sumPaidTotalAmountBySellerId(@Param("sellerId") Long sellerId);


    @Query(value = """
        SELECT COUNT(p.id)
        FROM payment p
        JOIN product pr ON p.product_id = pr.id
        WHERE pr.seller_id = :sellerId
          AND p.status = 'PAID'
    """, nativeQuery = true)
    long countPaidOrderBySellerId(@Param("sellerId") Long sellerId);


    @Query(value = """
        SELECT COALESCE(SUM(p.total_amount), 0)
        FROM payment p
        JOIN product pr ON p.product_id = pr.id
        WHERE pr.seller_id = :sellerId
          AND p.status = 'PAID'
          AND DATE(p.updated_at) BETWEEN :startDate AND :endDate
    """, nativeQuery = true)
    BigDecimal sumPaidTotalAmountBySellerIdAndUpdatedAtBetween(
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    @Query(value = """
        SELECT COUNT(p.id)
        FROM payment p
        JOIN product pr ON p.product_id = pr.id
        WHERE pr.seller_id = :sellerId
          AND p.status = 'PAID'
          AND DATE(p.updated_at) BETWEEN :startDate AND :endDate
    """, nativeQuery = true)
    long countPaidOrderBySellerIdAndUpdatedAtBetween(
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    @Query(value = """
        SELECT
            DATE_FORMAT(p.updated_at, '%Y-%m-%d') AS sales_date,
            COALESCE(SUM(p.total_amount), 0) AS daily_amount,
            COUNT(p.id) AS daily_count
        FROM payment p
        JOIN product pr ON p.product_id = pr.id
        WHERE pr.seller_id = :sellerId
          AND p.status = 'PAID'
          AND DATE(p.updated_at) BETWEEN :startDate AND :endDate
        GROUP BY DATE_FORMAT(p.updated_at, '%Y-%m-%d')
        ORDER BY sales_date ASC
    """, nativeQuery = true)
    List<Object[]> findDailyPaidSalesRowsBySellerIdAndUpdatedAtBetween(
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    @Query(value = """
        SELECT
            p.product_name AS product_name,
            COALESCE(SUM(p.total_amount), 0) AS amount,
            COUNT(p.id) AS payment_count
        FROM payment p
        JOIN product pr ON p.product_id = pr.id
        WHERE pr.seller_id = :sellerId
          AND p.status = 'PAID'
          AND DATE(p.updated_at) BETWEEN :startDate AND :endDate
        GROUP BY p.product_name
        ORDER BY payment_count DESC, amount DESC
        LIMIT 5
    """, nativeQuery = true)
    List<Object[]> findTopPaidProductRowsBySellerIdAndUpdatedAtBetween(
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
