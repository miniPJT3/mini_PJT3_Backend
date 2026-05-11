package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.Member;
import mini_pjt3.com.team1.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findAllByOrderByIdAsc();

    List<Member> findAllByRoleOrderByIdAsc(Role role);

    long countByRole(Role role);
}