package mini_pjt3.com.team1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.request.LoginRequest;
import mini_pjt3.com.team1.dto.request.MemberJoinRequest;
import mini_pjt3.com.team1.dto.response.ApiResponse;
import mini_pjt3.com.team1.dto.response.MemberResponse;
import mini_pjt3.com.team1.dto.response.TokenResponse;
import mini_pjt3.com.team1.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberResponse>> signup(
            @Valid @RequestBody MemberJoinRequest request
    ) {
        MemberResponse response = authService.signup(request);

        return ResponseEntity.ok(
                ApiResponse.success("회원가입 성공", response)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        TokenResponse tokenResponse = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("로그인 성공", tokenResponse)
        );
    }
}