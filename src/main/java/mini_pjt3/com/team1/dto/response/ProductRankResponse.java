package mini_pjt3.com.team1.dto.response;

import java.math.BigDecimal;

public record ProductRankResponse(
        int rank,
        String productName,
        BigDecimal amount,
        long count
) {
}