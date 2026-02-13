package com.example.finalproject.user.repository;

import com.example.finalproject.user.domain.SocialLogin;
import com.example.finalproject.user.enums.SocialProvider;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {
    Optional<SocialLogin> findByProviderAndProviderUserIdAndDeletedAtIsNull(SocialProvider provider, String providerUserId);

    @Modifying
    @Query("UPDATE SocialLogin sl SET sl.deletedAt = :deletedAt WHERE sl.user.id = :userId AND sl.deletedAt IS NULL")
    void softDeleteAllByUserId(@Param("userId") Long userId, @Param("deletedAt") java.time.LocalDateTime deletedAt);
}
