package com.example.finalproject.admin.dto.user;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserListResponse {
    private List<AdminUserListItemResponse> content;
    private Stats stats;
    private PageInfo page;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Stats {
        private long total;
        private long active;
        private long suspended;
        private long newThisMonth;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PageInfo {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
    }
}
