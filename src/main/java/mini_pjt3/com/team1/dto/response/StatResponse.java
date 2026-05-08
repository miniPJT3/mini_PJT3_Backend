package mini_pjt3.com.team1.dto.response;

import lombok.Builder;
import lombok.Getter;
import mini_pjt3.com.team1.entity.SellerSalesStat;

@Getter
@Builder
public class StatResponse {

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