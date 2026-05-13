package mini_pjt3.com.team1.dto.response;

public record AdminAccountRoleCountResponse(
        long totalCount,
        long userCount,
        long sellerCount,
        long adminCount
) {
}