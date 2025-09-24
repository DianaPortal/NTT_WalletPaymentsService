package com.nttdata.WalletPaymentsService.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardBalanceRequest {
    private String eventId;
    private String correlationId;
    private String traceId;
    private String walletId;
    private String phone;
    private String debitCardId;
    private Instant occurredAt;

}