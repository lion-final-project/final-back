package com.example.finalproject.moderation.repository;

import com.example.finalproject.moderation.domain.Report;
import com.example.finalproject.moderation.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("""
            SELECT r
            FROM Report r
            WHERE (:status IS NULL OR r.status = :status)
              AND (
                    :keyword IS NULL OR :keyword = ''
                    OR LOWER(r.reasonDetail) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(r.reporter.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(r.target.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR (r.storeOrder.order.orderNumber IS NOT NULL
                        AND LOWER(r.storeOrder.order.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))
                  )
            """)
    Page<Report> searchForAdmin(@Param("keyword") String keyword,
                                @Param("status") ReportStatus status,
                                Pageable pageable);

    long countByStatus(ReportStatus status);
}

