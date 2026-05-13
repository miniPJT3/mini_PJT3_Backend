package mini_pjt3.com.team1.dto.response;

import java.math.BigDecimal;

public record DailySalesResponse(
        String date,
        BigDecimal dailyAmount,
        long dailyCount
) {
}