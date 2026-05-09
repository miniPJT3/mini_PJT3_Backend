package mini_pjt3.com.team1.controller;

import mini_pjt3.com.team1.dto.response.StatResponse;
import mini_pjt3.com.team1.service.StatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final StatService statService;

    public DashboardController(StatService statService) {
        this.statService = statService;
    }

    @GetMapping("/sellers/{sellerId}/sales")
    public StatResponse getSellerSalesStat(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "DAILY") String period
    ) {
        return statService.getSellerSalesStat(sellerId, period);
    }
}