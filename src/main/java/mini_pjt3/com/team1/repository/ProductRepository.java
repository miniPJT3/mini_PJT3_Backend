package mini_pjt3.com.team1.repository;

import mini_pjt3.com.team1.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // 필요한 경우 판매자 ID로 필터링하거나 전체를 가져옵니다.
    List<Product> findAll();
}
