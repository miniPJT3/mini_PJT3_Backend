package mini_pjt3.com.team1.controller;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.response.AdminSystemStatusResponse;
import mini_pjt3.com.team1.service.AdminSystemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminSystemController {

    // 인터페이스 주입
    private final AdminSystemService adminSystemService;

    @GetMapping("/system-status")
    public ResponseEntity<AdminSystemStatusResponse> getSystemStatus() {
        AdminSystemStatusResponse response = adminSystemService.getSystemStatus();
        return ResponseEntity.ok(response);
    }
}