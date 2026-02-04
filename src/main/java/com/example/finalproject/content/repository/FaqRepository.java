package com.example.finalproject.content.repository;

import com.example.finalproject.content.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq, Long> {
}
