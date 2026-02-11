package com.example.finalproject.user.withdrawal.rule;

import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.withdrawal.dto.BlockedReason;
import java.util.Optional;

public interface WithdrawalEligibilityRule {

    Optional<BlockedReason> validate(User user);
}
