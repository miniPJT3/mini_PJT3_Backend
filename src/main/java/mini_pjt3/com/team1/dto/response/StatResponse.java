package mini_pjt3.com.team1.dto.response;

import java.time.LocalDate;
import java.util.List;

public record StatResponse(
        Long sellerId,
        String period,
        LocalDate startDate,
        LocalDate endDate,
        Long totalAmount,
        Integer totalCount,
        List<DailySalesResponse> dailySales,
        List<ProductRankResponse> topProducts
) {
}