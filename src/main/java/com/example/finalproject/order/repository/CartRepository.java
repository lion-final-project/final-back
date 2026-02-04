package com.example.finalproject.order.repository;

import com.example.finalproject.order.domain.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserEmail(String username);

    Optional<Cart> findByUserId(Long userId);
}
