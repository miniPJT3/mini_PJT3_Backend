package mini_pjt3.com.team1.service;

import mini_pjt3.com.team1.dto.response.DailySalesResponse;
import mini_pjt3.com.team1.dto.response.ProductRankResponse;
import mini_pjt3.com.team1.dto.response.StatResponse;
import mini_pjt3.com.team1.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StatService {

    private static final Long FIXED_SELLER_ID = 10L;

    private final PaymentRepository paymentRepository;

    public StatService( PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public StatResponse getSellerSalesStat(
            String period,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Long sellerId = FIXED_SELLER_ID;

        BigDecimal totalAmount =
                paymentRepository.sumPaidTotalAmountBySellerId(sellerId);

        long totalCount =
                paymentRepository.countPaidOrderBySellerId(sellerId);

        long customerCount =
               paymentRepository.countDistinctPaidCustomerBySellerId(sellerId);

        BigDecimal periodTotalAmount =
                paymentRepository.sumPaidTotalAmountBySellerIdAndUpdatedAtBetween(
                        sellerId,
                        startDate,
                        endDate
                );

        long periodTotalCount =
                paymentRepository.countPaidOrderBySellerIdAndUpdatedAtBetween(
                        sellerId,
                        startDate,
                        endDate
                );

        List<Object[]> dailyRows =
                paymentRepository.findDailyPaidSalesRowsBySellerIdAndUpdatedAtBetween(
                        sellerId,
                        startDate,
                        endDate
                );

        List<DailySalesResponse> dailySales = dailyRows.stream()
                .map(row -> new DailySalesResponse(
                        toStringValue(row[0]),
                        toBigDecimal(row[1]),
                        toLong(row[2])
                ))
                .toList();

        List<Object[]> productRows =
                paymentRepository.findTopPaidProductRowsBySellerIdAndUpdatedAtBetween(
                        sellerId,
                        startDate,
                        endDate
                );

        AtomicInteger rank = new AtomicInteger(1);

        List<ProductRankResponse> topProducts = productRows.stream()
                .map(row -> new ProductRankResponse(
                        rank.getAndIncrement(),
                        toStringValue(row[0]),
                        toBigDecimal(row[1]),
                        toLong(row[2])
                ))
                .toList();

        StatResponse.FixedSummary fixedSummary = new StatResponse.FixedSummary(
                nullToZero(totalAmount),
                totalCount,
                customerCount
        );

        return new StatResponse(
                sellerId,
                period,
                startDate,
                endDate,
                fixedSummary,
                nullToZero(periodTotalAmount),
                periodTotalCount,
                dailySales,
                topProducts
        );
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String toStringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.longValue());
        }

        return new BigDecimal(value.toString());
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.parseLong(value.toString());
    }
}