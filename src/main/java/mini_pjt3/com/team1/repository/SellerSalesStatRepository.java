package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.SellerSalesStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerSalesStatRepository extends JpaRepository<SellerSalesStat, Long> {

    Optional<SellerSalesStat> findBySellerId(Long sellerId);
    Optional<SellerSalesStat> findBySellerIdAndStatDate(Long sellerId, String statDate);

    boolean existsBySellerId(Long sellerId);
    boolean existsBySellerIdAndStatDate(Long sellerId, String statDate);
}