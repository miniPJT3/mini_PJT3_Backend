package mini_pjt3.com.team1.controller;

import mini_pjt3.com.team1.dto.response.AdminSystemStatusResponse;
import mini_pjt3.com.team1.service.AdminSystemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/system")
public class AdminSystemController {

    private final AdminSystemService adminSystemService;

    public AdminSystemController(AdminSystemService adminSystemService) {
        this.adminSystemService = adminSystemService;
    }

    @GetMapping("/status")
    public AdminSystemStatusResponse getSystemStatus() {
        return adminSystemService.getSystemStatus();
    }
}