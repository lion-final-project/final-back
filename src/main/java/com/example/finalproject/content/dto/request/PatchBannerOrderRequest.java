package com.example.finalproject.content.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PatchBannerOrderRequest {

    private List<Long> bannerIds;
}

