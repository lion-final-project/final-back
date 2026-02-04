package com.example.finalproject.user.repository;

import com.example.finalproject.user.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

// 사용자-역할 매핑 리포지토리.
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    // 사용자/역할 조합이 이미 존재하는지 확인.
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
}
