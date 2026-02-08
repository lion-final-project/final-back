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

    @Query("select a from Address a join a.user u where u.email = :email")
    List<Address> findByUserEmail(@Param("email") String email);

    @Query("SELECT a FROM Address a JOIN FETCH a.user WHERE a.id = :id")
    Optional<Address> findByIdWithUser(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user = :user AND a.isDefault = true")
    void clearDefaultByUser(@Param("user") User user);
}
