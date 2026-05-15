package mini_pjt3.com.team1.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.entity.Member;
import mini_pjt3.com.team1.enums.Role;
import mini_pjt3.com.team1.repository.MemberRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        //구글 고유 식별자(sub) 및 이메일 추출
        String providerId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        
        // DB에서 사용자 조회 (providerId 우선, 없으면 email로 조회)
        Optional<Member> memberOpt = memberRepository.findByProviderId(providerId);
        
        if (memberOpt.isEmpty() && email != null) {
            memberOpt = memberRepository.findByEmail(email);
        }
        
        String baseUrl = "http://localhost:5173";
        String targetUrl;

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();

            // DB에 provider_id가 비어있다면(첫 구글 로그인) 정보 업데이트.
            if (member.getProviderId() == null || member.getProviderId().isEmpty()) {
                member.setProvider("GOOGLE");
                member.setProviderId(providerId);
                memberRepository.save(member);
            }

            // JWT 토큰 생성 및 쿠키 설정
            String token = jwtUtil.createToken(authentication); 

            ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                    .path("/")
                    .httpOnly(true)
                    .secure(false) // HTTP 환경이므로 false
                    .maxAge(3600)
                    .sameSite("Lax")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // 리다이렉트 분기 로직
            // DB의 Role 필드에 GUEST가 정의되어 있지 않다면 에러가 날 수 있으므로 체크
            if (member.getPhone() == null || member.getPhone().isEmpty()) {
                // 전화번호가 없으면 추가 정보 입력 페이지로 이동
                targetUrl = baseUrl + "/additional-info";
            } else {
                // 역할에 따른 대시보드 이동
                targetUrl = determineTargetUrlByRole(baseUrl, member.getRole());
            }
        } else {
            // 가입되지 않은 구글 계정인 경우
            targetUrl = baseUrl + "/login?error=user_not_found";
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * 역할에 따른 타겟 URL 결정 헬퍼 메서드
     */
    private String determineTargetUrlByRole(String baseUrl, Role role) {
        // null 체크를 추가하여 안전하게 처리
        if (role == null) return baseUrl + "/user/home";

        switch (role) {
            case ADMIN:
                return baseUrl + "/admin/dashboard";
            case SELLER:
                return baseUrl + "/seller/dashboard";
            case USER:
            default:
                return baseUrl + "/user/home";
        }
    }
}