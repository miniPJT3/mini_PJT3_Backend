package mini_pjt3.com.team1.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.config.JwtUtil;
import mini_pjt3.com.team1.dto.request.LoginRequest;
import mini_pjt3.com.team1.dto.request.MemberJoinRequest;
import mini_pjt3.com.team1.dto.response.MemberResponse;
import mini_pjt3.com.team1.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    // 자체 회원가입
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody MemberJoinRequest request) {
        authService.join(request);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    // 일반 로그인
    @PostMapping("/login")
    public ResponseEntity<MemberResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        MemberResponse memberResponse = authService.login(request);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                memberResponse.getEmail(),
                "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + memberResponse.getRole()))
        );

        String token = jwtUtil.createToken(authentication);

        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .path("/")
                .httpOnly(true)
                .secure(false) 
                .maxAge(3600)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(memberResponse);
    }

    /**
     *로그아웃 API 추가
     * 서버 측에서 쿠키를 만료시켜 클라이언트의 토큰을 무효화
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // 동일한 이름의 쿠키를 생성하되 maxAge를 0으로 설정하여 즉시 삭제 유도
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(0) 
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}