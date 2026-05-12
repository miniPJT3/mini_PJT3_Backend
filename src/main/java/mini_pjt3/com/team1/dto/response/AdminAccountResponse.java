package mini_pjt3.com.team1.dto.response;

import mini_pjt3.com.team1.entity.Member;
import mini_pjt3.com.team1.enums.Role;

import java.time.LocalDateTime;

public record AdminAccountResponse(
        Long id,
        String email,
        String username,
        String name,
        String provider,
        Role role,
        String roleName,
        LocalDateTime createdAt
) {
    public static AdminAccountResponse from(Member member) {
        return new AdminAccountResponse(
                member.getId(),
                member.getEmail(),
                member.getLoginId(),
                member.getName(),
                member.getProvider(),
                member.getRole(),
                member.getRole() == null ? null : member.getRole().getTitle(),
                member.getCreatedAt()
        );
    }
}