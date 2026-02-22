package com.example.finalproject.admin.dto.user;

import com.example.finalproject.user.enums.UserStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserListItemResponse {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private UserStatus status;
    private Boolean isActive;
    private Integer addressCount;
    private Long orderCount;
    private LocalDateTime joinedAt;
}
