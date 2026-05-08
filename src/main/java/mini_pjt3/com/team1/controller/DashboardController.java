package mini_pjt3.com.team1.controller;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.response.ApiResponse;
import mini_pjt3.com.team1.dto.response.StatResponse;
import mini_pjt3.com.team1.service.StatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final StatService statService;

    @GetMapping("/statistics/{sellerId}")
    public ResponseEntity<ApiResponse<StatResponse>> getSellerStat(@PathVariable Long sellerId) {
        StatResponse response = statService.getSellerStat(sellerId);

        return ResponseEntity.ok(
                ApiResponse.success("통계 조회 성공", response)
        );
    }

    @GetMapping("/sales/{sellerId}")
    public ResponseEntity<ApiResponse<StatResponse>> initializeStat(@PathVariable Long sellerId) {

        StatResponse response = statService.initializeStat(sellerId);

        return ResponseEntity.ok(
                ApiResponse.success("매출 조회 성공", response)
        );
    }
}