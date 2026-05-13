package mini_pjt3.com.team1.service.impl;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.response.AdminAccountResponse;
import mini_pjt3.com.team1.dto.response.AdminAccountRoleCountResponse;
import mini_pjt3.com.team1.enums.Role;
import mini_pjt3.com.team1.repository.MemberRepository;
import mini_pjt3.com.team1.service.AdminAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAccountServiceImpl implements AdminAccountService {

    private final MemberRepository memberRepository;

    @Override
    public List<AdminAccountResponse> getAccounts(Role role) {
        // 🥊 Role이 있으면 필터링, 없으면 전체 조회 (ID 오름차순 정렬 유지)
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

    @Override
    public AdminAccountRoleCountResponse getRoleCounts() {
        long userCount = memberRepository.countByRole(Role.USER);
        long sellerCount = memberRepository.countByRole(Role.SELLER);
        long adminCount = memberRepository.countByRole(Role.ADMIN);

        // 🥊 전체 카운트를 각각 더해서 계산 (정확한 합계 보장)
        return new AdminAccountRoleCountResponse(
                userCount + sellerCount + adminCount,
                userCount,
                sellerCount,
                adminCount
        );
    }
}