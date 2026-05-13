package mini_pjt3.com.team1.service;

import mini_pjt3.com.team1.dto.response.AdminAccountResponse;
import mini_pjt3.com.team1.dto.response.AdminAccountRoleCountResponse;
import mini_pjt3.com.team1.enums.Role;
import java.util.List;

public interface AdminAccountService {
    List<AdminAccountResponse> getAccounts(Role role);
    AdminAccountRoleCountResponse getRoleCounts();
}