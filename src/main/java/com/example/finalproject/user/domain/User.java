package com.example.finalproject.user.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.user.enums.UserStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean termsAgreed = false;

    @Column(nullable = false)
    private Boolean privacyAgreed = false;

    private LocalDateTime termsAgreedAt;

    private LocalDateTime privacyAgreedAt;

    private LocalDateTime deletedAt;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> userRoles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();


    @Builder
    public User(String email, String password, String name, String phone,
                Boolean termsAgreed, Boolean privacyAgreed,
                LocalDateTime termsAgreedAt, LocalDateTime privacyAgreedAt) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.termsAgreed = termsAgreed != null ? termsAgreed : false;
        this.privacyAgreed = privacyAgreed != null ? privacyAgreed : false;
        this.termsAgreedAt = termsAgreedAt;
        this.privacyAgreedAt = privacyAgreedAt;
    }

    public boolean isAdmin() {
        return userRoles.stream()
                .anyMatch(ur -> ur.getRole().getRoleName().equals("ADMIN"));
    }
}
