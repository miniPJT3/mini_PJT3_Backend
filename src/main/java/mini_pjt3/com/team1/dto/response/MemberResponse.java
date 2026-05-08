package mini_pjt3.com.team1.dto.response;

import mini_pjt3.com.team1.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {

    private Long memberId;
    private String email;
    private String nickname;
    private String role;

    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getName())
                .role(member.getRole().name())
                .build();
    }
}