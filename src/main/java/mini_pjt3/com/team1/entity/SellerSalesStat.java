package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "seller_sales_stat")
public class SellerSalesStat extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sellerId; // nullable in DB

    private String statDate; // nullable in DB

    private String bottomProduct;
    private Long dailyAmount;
    private Integer dailyCount;
    private String topProduct;

    @Column(columnDefinition = "bigint default 0")
    private Long totalSales = 0L;
    @Column(columnDefinition = "bigint default 0")
    private Long todaySales = 0L;
    @Column(columnDefinition = "bigint default 0")
    private Long paymentCount = 0L;
    @Column(columnDefinition = "bigint default 0")
    private Long successCount = 0L;

    @Builder
    public SellerSalesStat(Long sellerId, String statDate, String bottomProduct, Long dailyAmount, Integer dailyCount, String topProduct) {
        this.sellerId = sellerId;
        this.statDate = statDate;
        this.bottomProduct = bottomProduct;
        this.dailyAmount = dailyAmount;
        this.dailyCount = dailyCount;
        this.topProduct = topProduct;
        this.totalSales = 0L;
        this.todaySales = 0L;
        this.paymentCount = 0L;
        this.successCount = 0L;
    }

    public void addSales(Long amount) {
        this.totalSales += amount;
        this.todaySales += amount;
        this.paymentCount++;
        this.successCount++;
    }
}