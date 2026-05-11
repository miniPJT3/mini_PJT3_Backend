package mini_pjt3.com.team1.service;

import mini_pjt3.com.team1.dto.response.AdminAccountResponse;
import mini_pjt3.com.team1.dto.response.AdminAccountRoleCountResponse;
import mini_pjt3.com.team1.enums.Role;
import mini_pjt3.com.team1.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminAccountService {

    private final MemberRepository memberRepository;

    public AdminAccountService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<AdminAccountResponse> getAccounts(Role role) {
        if (role != null) {
            return memberRepository.findAllByRoleOrderByIdAsc(role)
                    .stream()
                    .map(AdminAccountResponse::from)
                    .toList();
        }

        return memberRepository.findAllByOrderByIdAsc()
                .stream()
                .map(AdminAccountResponse::from)
                .toList();
    }

    public AdminAccountRoleCountResponse getRoleCounts() {
        long userCount = memberRepository.countByRole(Role.USER);
        long sellerCount = memberRepository.countByRole(Role.SELLER);
        long adminCount = memberRepository.countByRole(Role.ADMIN);

        return new AdminAccountRoleCountResponse(
                userCount + sellerCount + adminCount,
                userCount,
                sellerCount,
                adminCount
        );
    }
}