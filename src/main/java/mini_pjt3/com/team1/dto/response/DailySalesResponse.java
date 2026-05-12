package mini_pjt3.com.team1.dto.response;

import mini_pjt3.com.team1.entity.SellerSalesStat;

public record DailySalesResponse(
        String statDate,
        Long dailyAmount,
        Integer dailyCount
) {
    public static DailySalesResponse from(SellerSalesStat stat) {
        return new DailySalesResponse(
                stat.getStatDate(),
                stat.getDailyAmount(),
                stat.getDailyCount()
        );
    }
}