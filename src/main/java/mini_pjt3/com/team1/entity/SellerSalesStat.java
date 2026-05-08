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

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false, unique = true)
    private String statDate;

    @ColumnDefault("0")
    private Long totalSales = 0L;
    @ColumnDefault("0")
    private Long todaySales = 0L;
    @ColumnDefault("0")
    private Long paymentCount = 0L;
    @ColumnDefault("0")
    private Long successCount = 0L;

    @Builder
    public SellerSalesStat(Long sellerId, String statDate) {
        this.sellerId = sellerId;
        this.statDate = statDate;
        this.totalSales = 0L;
        this.todaySales = 0L;
        this.paymentCount = 0L;
        this.successCount = 0L;
    }

    public void init(Long sellerId, String statDate) {
        this.sellerId = sellerId;
        this.statDate = statDate;
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