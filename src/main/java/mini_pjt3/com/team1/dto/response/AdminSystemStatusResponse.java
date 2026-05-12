package mini_pjt3.com.team1.dto.response;

public record AdminSystemStatusResponse(
        long activeVirtualAccountCount,
        long todayTotalOrderCount,
        long todayPaidOrderCount,
        long todayFailedPaymentCount,
        double todayPaymentSuccessRate
) {
}