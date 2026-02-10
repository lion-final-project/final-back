package com.example.finalproject.admin.store.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminStoreListResponse {
    private AdminStoreStatsResponse stats;
    private List<AdminStoreListItemResponse> content;
    private AdminStorePageInfo page;
}
