package mini_pjt3.com.team1.controller;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.response.AdminAccountResponse;
import mini_pjt3.com.team1.dto.response.AdminAccountRoleCountResponse;
import mini_pjt3.com.team1.enums.Role;
import mini_pjt3.com.team1.service.AdminAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor // 🥊 생성자 주입 자동 생성
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // 🥊 CORS 에러 방지
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    /**
     * 전체 계정 리스트 조회
     * GET /api/admin/accounts
     * Role 파라미터가 있으면 필터링, 없으면 전체 조회
     */
    @GetMapping
    public ResponseEntity<List<AdminAccountResponse>> getAccounts(
            @RequestParam(required = false) Role role
    ) {
        List<AdminAccountResponse> response = adminAccountService.getAccounts(role);
        return ResponseEntity.ok(response);
    }

    /**
     * 역할별 계정 수 조회
     * GET /api/admin/accounts/role-counts
     */
    @GetMapping("/role-counts")
    public ResponseEntity<AdminAccountRoleCountResponse> getRoleCounts() {
        AdminAccountRoleCountResponse response = adminAccountService.getRoleCounts();
        return ResponseEntity.ok(response);
    }
}