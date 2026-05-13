package mini_pjt3.com.team1.controller;

import lombok.RequiredArgsConstructor;
import mini_pjt3.com.team1.entity.Product;
import mini_pjt3.com.team1.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<List<Product>> getProducts() {
        // DB의 상품 6개를 모두 가져와서 반환합니다.
        return ResponseEntity.ok(productRepository.findAll());
    }
}