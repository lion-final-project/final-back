package com.example.finalproject.user.repository;

import com.example.finalproject.user.domain.UserStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatusHistoryRepository extends JpaRepository<UserStatusHistory, Long> {
    List<UserStatusHistory> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}
