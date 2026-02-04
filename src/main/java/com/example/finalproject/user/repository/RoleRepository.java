package com.example.finalproject.user.repository;

import com.example.finalproject.user.domain.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// 역할 조회용 리포지토리.
public interface RoleRepository extends JpaRepository<Role, Long> {

    // 역할명으로 단건 조회.
    Optional<Role> findByRoleName(String roleName);
}
