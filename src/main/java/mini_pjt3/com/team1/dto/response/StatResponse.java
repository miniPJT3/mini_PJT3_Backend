package mini_pjt3.com.team1.dto.response;

import lombok.Builder;
import lombok.Getter;
import mini_pjt3.com.team1.entity.SellerSalesStat;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class StatResponse {
        Long sellerId;
        String period;
        LocalDate startDate;
        LocalDate endDate;
        Long totalAmount;
        Integer totalCount;
        List<DailySalesResponse> dailySales;
        List<ProductRankResponse> topProducts;

        private Long totalSales;
        private Long todaySales;
        private Long paymentCount;
        private Long successCount;

        public static StatResponse from(SellerSalesStat stat) {
                return StatResponse.builder()
                        .totalSales(stat.getTotalSales())
                        .todaySales(stat.getTodaySales())
                        .paymentCount(stat.getPaymentCount())
                        .successCount(stat.getSuccessCount())
                        .build();
        }
}