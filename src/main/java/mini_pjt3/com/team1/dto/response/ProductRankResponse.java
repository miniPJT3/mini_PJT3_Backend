package mini_pjt3.com.team1.dto.response;

public record ProductRankResponse(
        Integer rank,
        String productName,
        Long salesAmount,
        Long salesCount
) {
}