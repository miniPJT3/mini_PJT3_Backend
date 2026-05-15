package mini_pjt3.com.team1.controller;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.entity.Member;
import mini_pjt3.com.team1.enums.Role;
import mini_pjt3.com.team1.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    /**
     * 현재 로그인한 사용자의 정보를 반환하는 API
     * 프론트엔드 Header.jsx에서 새로고침 시 유저 정보(이름, 역할)를 복구하기 위해 사용
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentMember(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("로그인 정보가 없습니다.");
        }

        String email;
        if (authentication.getPrincipal() instanceof OAuth2User) {
            email = ((OAuth2User) authentication.getPrincipal()).getAttribute("email");
        } else {
            email = authentication.getName();
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 프론트엔드 useAuthStore의 userInfo 구조에 맞게 반환
        return ResponseEntity.ok(Map.of(
            "name", member.getName(),
            "role", member.getRole().name(), // "USER", "SELLER", "ADMIN", "GUEST"
            "email", member.getEmail()
        ));
    }

    /**
     * 최초 소셜 로그인 후 추가 정보(전화번호, 역할)를 저장하는 API
     */
    @PatchMapping("/additional-info")
    public ResponseEntity<?> updateAdditionalInfo(
            Authentication authentication, 
            @RequestBody Map<String, String> requestData) {

        if (authentication == null) {
            return ResponseEntity.status(401).body("인증 정보가 없습니다.");
        }

        String email;
        if (authentication.getPrincipal() instanceof OAuth2User) {
            email = ((OAuth2User) authentication.getPrincipal()).getAttribute("email");
        } else {
            email = authentication.getName();
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 회원을 찾을 수 없습니다."));

        String phone = requestData.get("phone");
        String roleStr = requestData.get("role"); 

        try {
            if (roleStr == null || roleStr.isEmpty()) {
                return ResponseEntity.badRequest().body("역할(Role) 정보가 누락되었습니다.");
            }

            // Enum 변환 및 정보 업데이트
            Role role = Role.valueOf(roleStr.toUpperCase());
            member.updateAdditionalInfo(phone, role);
            
            memberRepository.save(member);

            // App.jsx 경로에 맞춘 리다이렉트 경로 설정
            String redirectPath = role == Role.SELLER ? "/seller/dashboard" : "/user/home";
            
            return ResponseEntity.ok(Map.of(
                "message", "추가 정보가 성공적으로 업데이트되었습니다.",
                "role", role.name(),
                "redirectUrl", redirectPath
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("잘못된 역할(Role) 설정입니다: " + roleStr);
        }
    }
}