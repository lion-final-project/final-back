package com.example.finalproject.admin.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminStorePageInfo {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
}
