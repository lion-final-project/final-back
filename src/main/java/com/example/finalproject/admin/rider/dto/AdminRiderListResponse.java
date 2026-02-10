package com.example.finalproject.admin.rider.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminRiderListResponse {
    private AdminRiderStatsResponse stats;
    private List<AdminRiderListItemResponse> content;
    private AdminRiderPageInfo page;
}
