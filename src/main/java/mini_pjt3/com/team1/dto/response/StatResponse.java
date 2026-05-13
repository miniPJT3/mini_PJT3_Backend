package mini_pjt3.com.team1.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StatResponse(
        Long sellerId,
        String period,
        LocalDate startDate,
        LocalDate endDate,
        FixedSummary fixedSummary,
        BigDecimal periodTotalAmount,
        long periodTotalCount,
        List<DailySalesResponse> dailySales,
        List<ProductRankResponse> topProducts
) {
    public record FixedSummary(
            BigDecimal totalAmount,
            long totalCount,
            long customerCount
    ) {
    }
}