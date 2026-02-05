package com.example.finalproject.store.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostStoreBusinessHourRequest {

    @NotNull(message = "요일은 필수입니다.")
    @Min(value = 0, message = "요일은 0(일요일)부터 6(토요일)까지입니다.")
    @Max(value = 6, message = "요일은 0(일요일)부터 6(토요일)까지입니다.")
    private Short dayOfWeek;

    private String openTime;

    private String closeTime;

    @NotNull(message = "휴무 여부는 필수입니다.")
    private Boolean isClosed = false;
}
