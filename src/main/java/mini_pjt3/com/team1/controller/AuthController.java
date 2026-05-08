package mini_pjt3.com.team1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.request.LoginRequest;
import mini_pjt3.com.team1.dto.request.MemberJoinRequest;
import mini_pjt3.com.team1.dto.response.MemberResponse;
import mini_pjt3.com.team1.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 자체 회원가입
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody MemberJoinRequest request) {
        authService.join(request);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    // 일반 로그인 (기초 단계에서는 성공 여부와 권한만 반환)
    @PostMapping("/login")
    public ResponseEntity<MemberResponse> login(@RequestBody LoginRequest request) {
        MemberResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
