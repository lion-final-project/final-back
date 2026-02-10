package com.example.finalproject.admin.rider.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminRiderStatsResponse {
    private long total;
    private long operating;
    private long unavailable;
    private long idCardPending;
}
