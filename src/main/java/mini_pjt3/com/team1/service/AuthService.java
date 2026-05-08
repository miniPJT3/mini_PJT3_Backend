package mini_pjt3.com.team1.service;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.request.LoginRequest;
import mini_pjt3.com.team1.dto.request.MemberJoinRequest;
import mini_pjt3.com.team1.dto.response.MemberResponse;
import mini_pjt3.com.team1.dto.response.TokenResponse;
import mini_pjt3.com.team1.entity.Member;
import mini_pjt3.com.team1.enums.Role;
import mini_pjt3.com.team1.repository.MemberRepository;
import mini_pjt3.com.team1.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public MemberResponse signup(MemberJoinRequest request) {
        validateDuplicateEmail(request.getEmail());

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .name(request.getNickname())
                .role(Role.USER)
                .build();

        memberRepository.save(member);

        return MemberResponse.from(member);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호 불일치");
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일");
        }
    }
}