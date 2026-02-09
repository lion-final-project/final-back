package com.example.finalproject.coupon.repository;

import com.example.finalproject.coupon.domain.Coupon;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    List<Coupon> findByUserIdOrderByIdAsc(Long userId);
}
