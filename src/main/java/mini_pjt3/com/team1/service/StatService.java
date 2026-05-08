package mini_pjt3.com.team1.service;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.dto.response.StatResponse;
import mini_pjt3.com.team1.entity.SellerSalesStat;
import mini_pjt3.com.team1.repository.SellerSalesStatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class StatService {

    private final SellerSalesStatRepository statRepository;

    @Transactional(readOnly = true)
    public StatResponse getSellerStat(Long sellerId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        SellerSalesStat stat = statRepository.findBySellerIdAndStatDate(sellerId, today)
                .orElseThrow(() -> new IllegalArgumentException("통계 없음"));

        return StatResponse.from(stat);
    }

    @Transactional
    public void updateDailyStat(Long sellerId, Long amount) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        SellerSalesStat stat = statRepository.findBySellerIdAndStatDate(sellerId, today)
                .orElseGet(() -> {
                    SellerSalesStat newStat = SellerSalesStat.builder()
                            .sellerId(sellerId)
                            .statDate(today)
                            .build();
                    return statRepository.save(newStat);
                });

        stat.addSales(amount);
    }

    @Transactional
    public StatResponse initializeStat(Long sellerId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        if (!statRepository.existsBySellerIdAndStatDate(sellerId, today)) {
            SellerSalesStat newStat = SellerSalesStat.builder()
                    .sellerId(sellerId)
                    .statDate(today)
                    .build();
            statRepository.save(newStat);
        }
        return getSellerStat(sellerId);
    }
}