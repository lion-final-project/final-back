package com.example.finalproject.communication.repository;

import com.example.finalproject.communication.domain.NotificationBroadcast;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationBroadcastRepository extends JpaRepository<NotificationBroadcast, Long> {
}

