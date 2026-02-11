package com.example.finalproject.user.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.user.enums.SocialProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "social_logins",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_social_provider_user",
                columnNames = {"provider", "provider_user_id"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialLogin extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_social_logins_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(nullable = false)
    private LocalDateTime connectedAt;

    private LocalDateTime deletedAt;

    @Builder
    public SocialLogin(User user, SocialProvider provider,
                       String providerUserId, LocalDateTime connectedAt) {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.connectedAt = connectedAt;
    }
}