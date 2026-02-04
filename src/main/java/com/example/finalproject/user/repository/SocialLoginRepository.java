package com.example.finalproject.user.repository;

import com.example.finalproject.user.domain.SocialLogin;
import com.example.finalproject.user.enums.SocialProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {
    Optional<SocialLogin> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);
}
