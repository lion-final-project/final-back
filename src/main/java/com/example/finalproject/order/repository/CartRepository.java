package com.example.finalproject.order.repository;

import com.example.finalproject.order.domain.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    /** User 엔티티의 email로 조회 (user.email) */
    Optional<Cart> findByUser_Email(String email);

    Optional<Cart> findByUserId(Long userId);
}
