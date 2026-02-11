package com.example.finalproject.user.repository;

import com.example.finalproject.user.domain.SocialLogin;
import com.example.finalproject.user.enums.SocialProvider;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {
    Optional<SocialLogin> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

    @Modifying
    @Query("DELETE FROM SocialLogin sl WHERE sl.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
