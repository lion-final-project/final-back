package com.example.finalproject.review.repository;

import com.example.finalproject.review.domain.Review;
import com.example.finalproject.review.dto.response.GetReviewStatisticsResponse;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByStoreOrder_Id(Long storeOrderId);

    @EntityGraph(attributePaths = {"user"})
    Optional<Review> findDetailById(Long id);

    @EntityGraph(attributePaths = {"user", "storeOrder"})
    Page<Review> findByStoreOrder_Store_IdAndIsVisibleTrue(Long storeId, Pageable pageable);


    @Query("SELECT "
            + "SUM(CASE WHEN r.rating = 5 THEN 1 ELSE 0 END), "
            + "SUM(CASE WHEN r.rating = 4 THEN 1 ELSE 0 END), "
            + "SUM(CASE WHEN r.rating = 3 THEN 1 ELSE 0 END), "
            + "SUM(CASE WHEN r.rating = 2 THEN 1 ELSE 0 END), "
            + "SUM(CASE WHEN r.rating = 1 THEN 1 ELSE 0 END), "
            + "AVG(r.rating) "
            + "FROM Review r "
            + "WHERE r.storeOrder.store.id = :storeId "
            + "AND r.isVisible = true")
    GetReviewStatisticsResponse getReviewStatistics(@Param("storeId") Long storeId);


    Optional<Review> findByStoreOrder_Id(Long storeOrderId);
}
