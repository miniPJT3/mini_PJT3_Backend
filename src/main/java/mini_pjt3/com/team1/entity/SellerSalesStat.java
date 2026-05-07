package mini_pjt3.com.team1.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerSalesStat extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sellerId;
    private String statDate;
    private Long dailyAmount;
    private Integer dailyCount;
    private String topProduct;
    private String bottomProduct;

    @Builder
    public SellerSalesStat(Long sellerId, String statDate, Long dailyAmount, Integer dailyCount, String topProduct, String bottomProduct) {
        this.sellerId = sellerId;
        this.statDate = statDate;
        this.dailyAmount = dailyAmount;
        this.dailyCount = dailyCount;
        this.topProduct = topProduct;
        this.bottomProduct = bottomProduct;
    }
}