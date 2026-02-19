package com.example.finalproject.admin.dto.report;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReportListResponse {
    private List<AdminReportListItemResponse> content;
    private Stats stats;
    private PageInfo page;

    @Getter
    @Builder
    public static class Stats {
        private long total;
        private long pending;
        private long resolved;
    }

    @Getter
    @Builder
    public static class PageInfo {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
    }
}
