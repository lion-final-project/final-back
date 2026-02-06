package com.example.finalproject.user.repository;

import com.example.finalproject.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Page<User>findAllBy(Pageable pageable);

    Page<User>findAllByDeletedAtIsNull(Pageable pageable);

    Page<User>findAllByDeletedAtIsNotNull(Pageable pageable);
}
