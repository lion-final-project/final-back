package com.example.finalproject.store.domain.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementAccount {

    @Column(name = "settlement_bank_name", nullable = false, length = 50)
    private String bankName;

    @Column(name = "settlement_bank_account", nullable = false, length = 255)
    private String bankAccount;

    @Column(name = "settlement_account_holder", nullable = false, length = 50)
    private String accountHolder;

    @Builder
    public SettlementAccount(String bankName, String bankAccount, String accountHolder) {
        this.bankName = bankName;
        this.bankAccount = bankAccount;
        this.accountHolder = accountHolder;
    }
}
