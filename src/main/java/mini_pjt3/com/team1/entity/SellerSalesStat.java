package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "seller_sales_stats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seller_stat_date",
                        columnNames = {"seller_id", "stat_date"}
                )
        },
        indexes = {
                @Index(name = "idx_seller_sales_seller_id", columnList = "seller_id"),
                @Index(name = "idx_seller_sales_stat_date", columnList = "stat_date"),
                @Index(name = "idx_seller_sales_seller_date", columnList = "seller_id, stat_date")
        }
)
public class SellerSalesStat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Member 테이블의 판매자 ID와 연동
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    // YYYY-MM-DD 형식
    @Column(name = "stat_date", nullable = false, length = 10)
    private String statDate;

    @Column(name = "bottom_product", length = 255)
    private String bottomProduct;

    // 해당 날짜의 총 매출액
    @Column(name = "daily_amount", nullable = false)
    private Long dailyAmount;

    // 해당 날짜의 총 결제 완료 건수
    @Column(name = "daily_count", nullable = false)
    private Integer dailyCount;

    // 해당 날짜 기준 대표 인기 상품
    @Column(name = "top_product", length = 255)
    private String topProduct;

    @Column(columnDefinition = "bigint default 0")
    private Long totalSales = 0L;
    @Column(columnDefinition = "bigint default 0")
    private Long todaySales = 0L;
    @Column(columnDefinition = "bigint default 0")
    private Long paymentCount = 0L;
    @Column(columnDefinition = "bigint default 0")
    private Long successCount = 0L;
    // 해당 날짜 기준 대표 비인기 상품
    @Column(name = "low_product", length = 255)
    private String lowProduct;

    @Builder
    public SellerSalesStat(
            Long sellerId,
            String statDate,
            Long dailyAmount,
            Integer dailyCount,
            String topProduct,
            String lowProduct
    ) {
        this.sellerId = sellerId;
        this.statDate = statDate;
        this.bottomProduct = bottomProduct;
        this.dailyAmount = dailyAmount;
        this.dailyCount = dailyCount;
        this.topProduct = topProduct;
        this.lowProduct = lowProduct;
    }

        public void addSales(Long amount) {
                this.totalSales += amount;
                this.todaySales += amount;
                this.paymentCount++;
                this.successCount++;
        }

    public void update(
            Long dailyAmount,
            Integer dailyCount,
            String topProduct,
            String lowProduct
    ) {
        this.dailyAmount = dailyAmount;
        this.dailyCount = dailyCount;
        this.topProduct = topProduct;
        this.lowProduct = lowProduct;
    }
}