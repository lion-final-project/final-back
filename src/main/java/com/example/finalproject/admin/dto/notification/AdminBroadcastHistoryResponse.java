package com.example.finalproject.admin.dto.notification;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminBroadcastHistoryResponse {
    private List<AdminBroadcastHistoryItemResponse> content;
    private PageInfo page;

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

