package com.example.finalproject.user.repository;

import com.example.finalproject.user.domain.Address;
import com.example.finalproject.user.domain.User;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserOrderByIsDefaultDesc(User user);

    List<Address> findByUserEmail(String email);

    @Query("SELECT a FROM Address a JOIN FETCH a.user WHERE a.id = :id")
    Optional<Address> findByIdWithUser(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user = :user AND a.isDefault = true")
    void clearDefaultByUser(@Param("user") User user);

    long countByUserId(Long userId);

    //같은 이름의 주소지명이 있는지 확인
    Boolean existsByUserIdAndAddressName(Long id, String addressName);

    //같은 주소가 있는지 확인
    Boolean existsByUserIdAndAddressLine1AndAddressLine2(Long id, String addressLine1, String addressLine2);

    Boolean existsByUserIdAndAddressNameAndIdNot(Long userId, String addressName, Long id);

    Boolean existsByUserIdAndAddressLine1AndAddressLine2AndIdNot(Long userId, String line1, String line2, Long id);

    Optional<Address> findFirstByUserIdOrderByCreatedAtDesc(Long id);
}
