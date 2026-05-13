package mini_pjt3.com.team1.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.config.JwtUtil; // JwtUtil 임포트
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
    private final JwtUtil jwtUtil; // 1. JwtUtil 주입

    // 자체 회원가입
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody MemberJoinRequest request) {
        authService.join(request);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    // 일반 로그인 수정
    @PostMapping("/login")
    public ResponseEntity<MemberResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        // 기존 서비스 로직 수행
        MemberResponse memberResponse = authService.login(request);

        // 2. 인증 객체 생성 (JwtUtil.createToken이 Authentication을 인자로 받으므로)
        // Password 부분은 보안상 빈 값으로 둡니다.
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                memberResponse.getEmail(),
                "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + memberResponse.getRole()))
        );

        // 3. JWT 토큰 생성
        String token = jwtUtil.createToken(authentication);

        // 4. 쿠키 생성 (OAuth2SuccessHandler와 동일한 설정)
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .path("/")
                .httpOnly(true)    // 보안: 자바스크립트 접근 불가
                .secure(false)     // 로컬 환경이므로 false
                .maxAge(3600)      // 1시간
                .sameSite("Lax")
                .build();

        // 5. 응답 헤더에 쿠키 추가
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(memberResponse);
    }
}