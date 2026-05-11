package mini_pjt3.com.team1.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.entity.Member;
import mini_pjt3.com.team1.enums.Role;
import mini_pjt3.com.team1.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // 프론트엔드 리다이렉트 URL의 ?role= 파라미터 추출
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String roleParam = request.getParameter("role");

        Member member = saveOrUpdate(email, name, registrationId, roleParam);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())),
                attributes,
                "email"
        );
    }

    private Member saveOrUpdate(String email, String name, String provider, String roleParam) {
        // 파라미터가 SELLER면 SELLER, 아니면 기본 USER 부여
        Role selectedRole = "SELLER".equalsIgnoreCase(roleParam) ? Role.SELLER : Role.USER;

        return memberRepository.findByEmail(email)
                .map(entity -> {
                    // 기존 회원은 이름과 제공자 정보를 업데이트하고 DB에 저장
                    entity.setName(name);
                    entity.setProvider(provider);
                    return memberRepository.save(entity);
                })
                .orElseGet(() -> {
                    //Member 엔티티의 필드명인 loginId를 사용하여 빌더를 호출
                    // return 키워드를 사용하여 저장된 Member 객체를 반환
                    return memberRepository.save(Member.builder()
                            .loginId(email)   // 엔티티 필드명에 맞춰 username에서 loginId로 변경
                            .email(email)
                            .name(name)
                            .provider(provider)
                            .role(selectedRole)
                            .build());
                });
    }
}