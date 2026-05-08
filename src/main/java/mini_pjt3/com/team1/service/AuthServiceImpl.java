package mini_pjt3.com.team1.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.request.LoginRequest;
import mini_pjt3.com.team1.dto.request.MemberJoinRequest;
import mini_pjt3.com.team1.dto.response.MemberResponse;
import mini_pjt3.com.team1.entity.Member;
import mini_pjt3.com.team1.enums.Role;
import mini_pjt3.com.team1.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에서 빈으로 등록해야 함

    @Override
    public void join(MemberJoinRequest request) {
        // 1. 아이디 중복 체크
        memberRepository.findByLoginId(request.getLoginId())
                .ifPresent(m -> { throw new IllegalStateException("이미 존재하는 아이디입니다."); });

        // 2. 비밀번호 암호화 및 저장
        Member member = Member.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 암호화 저장
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(Role.valueOf(request.getRole())) // USER 또는 SELLER
                .build();

        memberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse login(LoginRequest request) {
        // 1. 아이디 확인
        Member member = memberRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디입니다."));

        // 2. 비밀번호 일치 확인
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 응답 객체(DTO) 생성 및 반환
        return MemberResponse.builder()
                .name(member.getName())
                .role(member.getRole().name())
                .email(member.getEmail())
                .build();
    }
}
