package com.example.finalproject.communication.repository;

import com.example.finalproject.communication.domain.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    int countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("update Notification n "
            + "set n.isRead = true, n.sentAt = current_timestamp "
            + "where n.user.id = :userId and n.isRead = false")
    void markAllReadByUserId(Long userId);

}
