package mini_pjt3.com.team1.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mini_pjt3.com.team1.dto.response.DailySalesResponse;
import mini_pjt3.com.team1.dto.response.ProductRankResponse;
import mini_pjt3.com.team1.dto.response.StatResponse;
import mini_pjt3.com.team1.entity.SellerSalesStat;
import mini_pjt3.com.team1.repository.SellerSalesStatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatService {

    private final SellerSalesStatRepository sellerSalesStatRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public StatService(SellerSalesStatRepository sellerSalesStatRepository) {
        this.sellerSalesStatRepository = sellerSalesStatRepository;
    }

    @Transactional(readOnly = true)
    public StatResponse getSellerSalesStat(Long sellerId, String period) {
        DateRange dateRange = calculateDateRange(period);

        List<SellerSalesStat> stats =
                sellerSalesStatRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
                        sellerId,
                        dateRange.startDate().toString(),
                        dateRange.endDate().toString()
                );

        List<DailySalesResponse> dailySales = stats.stream()
                .map(DailySalesResponse::from)
                .toList();

        Long totalAmount = stats.stream()
                .mapToLong(SellerSalesStat::getDailyAmount)
                .sum();

        Integer totalCount = stats.stream()
                .mapToInt(SellerSalesStat::getDailyCount)
                .sum();

        List<ProductRankResponse> topProducts = findTopProducts(
                sellerId,
                dateRange.startDate(),
                dateRange.endDate()
        );

        return new StatResponse(
                sellerId,
                normalizePeriod(period),
                dateRange.startDate(),
                dateRange.endDate(),
                totalAmount,
                totalCount,
                dailySales,
                topProducts
        );
    }

    private List<ProductRankResponse> findTopProducts(
            Long sellerId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        String sql = """
                SELECT
                    product_name,
                    COALESCE(SUM(total_amount), 0) AS sales_amount,
                    COUNT(*) AS sales_count
                FROM payments
                WHERE seller_id = :sellerId
                  AND status = 'PAID'
                  AND DATE(created_at) BETWEEN :startDate AND :endDate
                GROUP BY product_name
                ORDER BY sales_amount DESC, sales_count DESC
                LIMIT 5
                """;

        List<?> rows = entityManager.createNativeQuery(sql)
                .setParameter("sellerId", sellerId)
                .setParameter("startDate", startDate.toString())
                .setParameter("endDate", endDate.toString())
                .getResultList();

        List<ProductRankResponse> responses = new ArrayList<>();

        int rank = 1;

        for (Object row : rows) {
            Object[] columns = (Object[]) row;

            String productName = String.valueOf(columns[0]);
            Long salesAmount = ((Number) columns[1]).longValue();
            Long salesCount = ((Number) columns[2]).longValue();

            responses.add(new ProductRankResponse(
                    rank,
                    productName,
                    salesAmount,
                    salesCount
            ));

            rank++;
        }

        return responses;
    }

    private DateRange calculateDateRange(String period) {
        LocalDate today = LocalDate.now();
        String normalizedPeriod = normalizePeriod(period);

        return switch (normalizedPeriod) {
            case "DAILY" -> new DateRange(today, today);

            case "WEEKLY" -> {
                LocalDate monday = today.with(DayOfWeek.MONDAY);
                LocalDate sunday = today.with(DayOfWeek.SUNDAY);
                yield new DateRange(monday, sunday);
            }

            case "MONTHLY" -> {
                LocalDate firstDay = today.withDayOfMonth(1);
                LocalDate lastDay = today.withDayOfMonth(today.lengthOfMonth());
                yield new DateRange(firstDay, lastDay);
            }

            default -> throw new IllegalArgumentException("지원하지 않는 조회 기간입니다. DAILY, WEEKLY, MONTHLY 중 하나를 사용하세요.");
        };
    }

    private String normalizePeriod(String period) {
        if (period == null || period.isBlank()) {
            return "DAILY";
        }

        String value = period.trim().toUpperCase();

        return switch (value) {
            case "DAILY", "DAY", "일" -> "DAILY";
            case "WEEKLY", "WEEK", "주" -> "WEEKLY";
            case "MONTHLY", "MONTH", "월" -> "MONTHLY";
            default -> value;
        };
    }

    private record DateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}