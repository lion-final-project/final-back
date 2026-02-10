package com.example.finalproject.content.domain;

import com.example.finalproject.content.enums.ContentStatus;
import com.example.finalproject.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "banners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Banner extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(name = "background_color", length = 50)
    private String backgroundColor;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status = ContentStatus.ACTIVE;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    @Builder
    public Banner(String title, String content, String imageUrl, String linkUrl,
                  String backgroundColor, Integer displayOrder, ContentStatus status,
                  LocalDateTime startedAt, LocalDateTime endedAt) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.backgroundColor = backgroundColor;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.status = status != null ? status : ContentStatus.ACTIVE;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public void update(String title, String content, String imageUrl, String linkUrl,
                       String backgroundColor, Integer displayOrder, ContentStatus status,
                       LocalDateTime startedAt, LocalDateTime endedAt) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (linkUrl != null) this.linkUrl = linkUrl;
        if (backgroundColor != null) this.backgroundColor = backgroundColor;
        if (displayOrder != null) this.displayOrder = displayOrder;
        if (status != null) this.status = status;
        if (startedAt != null) this.startedAt = startedAt;
        if (endedAt != null) this.endedAt = endedAt;
    }
}