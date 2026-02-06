package com.example.finalproject.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PostAddressCreateRequest {

    @NotBlank
    private String addressName;

    @NotBlank
    private String postalCode;

    @NotBlank
    private String addressLine1;

    private String addressLine2;

    @NotBlank
    private String contact;

    private Boolean isDefault;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}
