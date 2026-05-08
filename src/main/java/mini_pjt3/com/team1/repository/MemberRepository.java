package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    
    // 이 부분을 추가하면 Spring Data JPA가 자동으로 SQL을 생성해줍니다.
    Optional<Member> findByLoginId(String loginId);
}