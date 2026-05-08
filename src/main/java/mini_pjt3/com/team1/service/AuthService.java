package mini_pjt3.com.team1.service;

import mini_pjt3.com.team1.dto.request.LoginRequest;
import mini_pjt3.com.team1.dto.request.MemberJoinRequest;
import mini_pjt3.com.team1.dto.response.MemberResponse;

public interface AuthService {
    // 일반 회원가입 로직
    void join(MemberJoinRequest request);
    
    // 일반 로그인 로직
    MemberResponse login(LoginRequest request);
}