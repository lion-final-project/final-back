package com.example.finalproject.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordResetRequest {

    @Email
    @NotBlank
    private String email;
}
