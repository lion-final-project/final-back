package com.example.finalproject.order.repository;

import com.example.finalproject.order.domain.CartProduct;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartProductRepository extends JpaRepository<CartProduct, Long> {
    Optional<CartProduct> findByCartIdAndProductId(Long cartId, Long productId);
    List<CartProduct> findAllByCartId(Long cartId);
    void deleteAllByCartId(Long cartId);
}
