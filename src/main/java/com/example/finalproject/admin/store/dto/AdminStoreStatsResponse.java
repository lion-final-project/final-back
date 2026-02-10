package com.example.finalproject.admin.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminStoreStatsResponse {
    private long total;
    private long active;
    private long inactive;
    private long pending;
}
