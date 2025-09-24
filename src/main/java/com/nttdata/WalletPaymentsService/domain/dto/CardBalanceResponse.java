package com.nttdata.WalletPaymentsService.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

// CardBalanceResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardBalanceResponse {
  private String correlationId;
  private String traceId;
  private String phone;
  private String debitCardId;
  private boolean success;
  private String reason;           // si falla
  private BigDecimal balance;    // saldo disponible
  private String currency;         // "PEN", "USD", etc.
  private Instant asOf;            // fecha/hora del saldo
}