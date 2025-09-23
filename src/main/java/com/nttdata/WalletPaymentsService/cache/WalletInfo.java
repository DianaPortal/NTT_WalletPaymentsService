package com.nttdata.walletpaymentsservice.cache;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class WalletInfo {

    private String walletId;
    private String phone;
    private String state;        // ACTIVE | BLOCKED | CLOSED
    private String linkedCardId; // opcional
    private BigDecimal cardBalance;
    private String cardCurrency;
    private Instant balanceAsOf;
}
