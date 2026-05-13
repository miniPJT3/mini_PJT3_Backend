package mini_pjt3.com.team1.controller;

import mini_pjt3.com.team1.dto.response.StatResponse;
import mini_pjt3.com.team1.service.StatService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final StatService statService;

    public DashboardController(StatService statService) {
        this.statService = statService;
    }

    @GetMapping("/seller-sales")
    public StatResponse getSellerSalesStat(
            @RequestParam(required = false, defaultValue = "10") Long sellerId,
            @RequestParam String period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return statService.getSellerSalesStat(
                period,
                startDate,
                endDate
        );
    }
}