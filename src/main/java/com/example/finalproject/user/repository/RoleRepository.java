package com.example.finalproject.user.repository;

import com.example.finalproject.user.domain.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // 역할명으로 Role 조회.
    Optional<Role> findByRoleName(String roleName);
}
