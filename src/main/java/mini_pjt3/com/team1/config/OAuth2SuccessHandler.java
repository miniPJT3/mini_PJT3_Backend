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
        
        // 구글 고유 식별자(sub) 및 이메일 추출
        String providerId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        
        // DB에서 사용자 조회 (providerId 우선, 없으면 email로 조회)
        Optional<Member> memberOpt = memberRepository.findByProviderId(providerId);
        
        if (memberOpt.isEmpty() && email != null) {
            memberOpt = memberRepository.findByEmail(email);
        }
        
        // 배포 환경의 프론트엔드 베이스 주소 (ALB 주소)
        String baseUrl = "http://team01-alb-1090661033.ap-northeast-2.elb.amazonaws.com";
        String targetUrl;

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();

            // 첫 구글 로그인 시 정보 업데이트
            if (member.getProviderId() == null || member.getProviderId().isEmpty()) {
                member.setProvider("GOOGLE");
                member.setProviderId(providerId);
                memberRepository.save(member);
            }

            // JWT 토큰 생성
            String token = jwtUtil.createToken(authentication); 

            //  배포 환경에서 브라우저가 쿠키를 온전하게 저장할 수 있도록 domain 설정을 명시
            ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                    .path("/")
                    .domain("team01-alb-1090661033.ap-northeast-2.elb.amazonaws.com") // ALB 도메인 명시
                    .httpOnly(true)
                    .secure(false) // HTTP 배포 환경이므로 false 유지 (향후 SSL/HTTPS 적용 시 true 변경)
                    .maxAge(3600)
                    .sameSite("Lax") // 크로스 도메인 리다이렉트 시 쿠키 유지 설정
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // 리다이렉트 분기 로직
            if (member.getPhone() == null || member.getPhone().isEmpty()) {
                // 추가 정보 입력이 필요한 경우
                targetUrl = baseUrl + "/additional-info";
            } else {
                // 역할에 따른 대시보드 이동
                targetUrl = determineTargetUrlByRole(baseUrl, member.getRole());
            }
        } else {
            // 회원가입이 되어 있지 않은 계정인 경우
            targetUrl = baseUrl + "/login?error=user_not_found";
        }

        // 세션에 남아있는 인증 관련 데이터 삭제 및 리다이렉트 실행
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * 역할에 따른 타겟 URL 결정 헬퍼 메서드
     */
    private String determineTargetUrlByRole(String baseUrl, Role role) {
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