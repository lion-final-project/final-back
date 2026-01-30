package com.example.finalproject.communication.domain;

import com.example.finalproject.communication.enums.BroadcastRefType;
import com.example.finalproject.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "notification_broadcasts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationBroadcast extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false,
            columnDefinition = "broadcast_ref_type DEFAULT 'ALL'")
    private BroadcastRefType referenceType = BroadcastRefType.ALL;

    @Builder
    public NotificationBroadcast(String title, String content,
                                 BroadcastRefType referenceType) {
        this.title = title;
        this.content = content;
        this.referenceType = referenceType != null ? referenceType : BroadcastRefType.ALL;
    }
}