package com.example.finalproject.global.storage.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreImageType {

    PROFILE("profile"),
    PRODUCT("product");

    private final String directory;
}
