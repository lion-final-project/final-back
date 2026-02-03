package com.example.finalproject.communication.repository;

import com.example.finalproject.communication.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
