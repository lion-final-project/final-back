package com.example.finalproject.user.repository;

import com.example.finalproject.user.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    // 사용자-역할 매핑 존재 여부 확인.
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
}
