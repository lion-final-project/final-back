package com.example.finalproject.payment.repository;

import com.example.finalproject.payment.domain.PaymentMethod;
import com.example.finalproject.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    Optional<PaymentMethod> findFirstByUserIdAndIsDefaultTrue(Long userId);

    List<PaymentMethod> findByUserOrderByIsDefaultDesc(User user);

    Optional<PaymentMethod> findByIdAndUser_Id(Long id, Long userId);
}
