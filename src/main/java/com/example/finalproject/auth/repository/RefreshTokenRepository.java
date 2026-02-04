package com.example.finalproject.auth.repository;

import com.example.finalproject.auth.domain.RefreshToken;
import com.example.finalproject.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUser(User user);
}
