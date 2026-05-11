package mini_pjt3.com.team1.controller;

import mini_pjt3.com.team1.dto.response.AdminAccountResponse;
import mini_pjt3.com.team1.dto.response.AdminAccountRoleCountResponse;
import mini_pjt3.com.team1.enums.Role;
import mini_pjt3.com.team1.service.AdminAccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    /**
     * 전체 계정 리스트 조회
     *
     * GET /api/admin/accounts
     */
    @GetMapping
    public List<AdminAccountResponse> getAccounts(
            @RequestParam(required = false) Role role
    ) {
        return adminAccountService.getAccounts(role);
    }

    /**
     * 역할별 계정 수 조회
     *
     * GET /api/admin/accounts/role-counts
     */
    @GetMapping("/role-counts")
    public AdminAccountRoleCountResponse getRoleCounts() {
        return adminAccountService.getRoleCounts();
    }
}