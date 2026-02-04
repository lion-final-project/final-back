package com.example.finalproject.content.repository;

import com.example.finalproject.content.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
