package com.example.finalproject.store.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreCategoryType {

    FRUIT_VEGETABLE("과일/채소"),
    MEAT_EGG("정육/계란"),
    SEAFOOD("수산/해산물"),
    SIDE_DISH("반찬/간편식"),
    SNACK("간식"),
    BAKERY("베이커리"),
    ETC("기타");

    private final String displayName;
}
