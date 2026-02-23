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
import java.time.format.DateTimeFormatter;
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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(nullable = false, columnDefinition = "integer not null default 0")
    private Integer points = 0;

    @Column(nullable = false, columnDefinition = "integer not null default 0")
    private Integer tokenVersion = 0;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<UserRole> userRoles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Address> addresses = new ArrayList<>();


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

    // 보유 포인트 설정 시드/추가 기능(적립,차감) 확장용
    public void setPoints(int points) {
        this.points = points >= 0 ? points : 0;
    }

    public void deactive() {
        deactive(LocalDateTime.now());
    }

    public void deactive(LocalDateTime deletedAt) {
        this.status = UserStatus.INACTIVE;
        this.deletedAt = deletedAt;
        increaseTokenVersion();
    }

    public void increaseTokenVersion() {
        this.tokenVersion = (this.tokenVersion == null ? 0 : this.tokenVersion) + 1;

    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void inactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public void maskPersonalInfoForWithdrawal(LocalDateTime deletedAt) {
        String timestamp = deletedAt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String key = "deleted_" + this.id + "_" + timestamp;
        this.email = key + "@example.com";
        String maskedPhone = "del" + this.id + timestamp;
        this.phone = maskedPhone.length() > 20 ? maskedPhone.substring(0, 20) : maskedPhone;
        this.name = "탈퇴회원_" + this.id;
    }

    public void changePassword(String newpassword) {
        this.password = newpassword;
    }
}
