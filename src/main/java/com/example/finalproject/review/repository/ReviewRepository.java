package com.example.finalproject.review.repository;

import com.example.finalproject.review.domain.Review;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByStoreOrder_Id(Long storeOrderId);

    @EntityGraph(attributePaths = {"user"})
    Optional<Review> findDetailById(Long id);

    Optional<Review> findByStoreOrder_Id(Long storeOrderId);
}
