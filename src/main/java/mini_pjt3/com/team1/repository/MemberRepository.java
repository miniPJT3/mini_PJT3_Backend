package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.Member;
import mini_pjt3.com.team1.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 구글 소셜 로그인 시 사용자의 이메일 정보를 바탕으로
     * 기존에 가입된 회원인지 확인하기 위해 사용
     */
    Optional<Member> findByEmail(String email);

    Optional<Member> findByProviderId(String providerId);
    
    /**
     * 일반 로그인 시 아이디를 통해 회원을 찾을 때 사용
     */
    Optional<Member> findByLoginId(String loginId);

    List<Member> findAllByOrderByIdAsc();

    List<Member> findAllByRoleOrderByIdAsc(Role role);

    long countByRole(Role role);
}
