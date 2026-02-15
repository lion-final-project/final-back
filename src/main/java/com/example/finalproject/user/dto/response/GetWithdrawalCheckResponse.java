package com.example.finalproject.user.dto.response;

import java.util.List;

import com.example.finalproject.user.withdrawal.dto.BlockedReason;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetWithdrawalCheckResponse {

    private final boolean canWithdraw;
    private final List<BlockedReason> blockedReasons;

    /** 컨트롤러 등에서 탈퇴 가능 여부 판단용 */
    public boolean isCanWithdraw() {
        return canWithdraw;
    }
}
