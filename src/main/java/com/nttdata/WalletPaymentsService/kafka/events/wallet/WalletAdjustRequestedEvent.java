package com.nttdata.WalletPaymentsService.kafka.events.wallet;

import lombok.*;

import java.math.*;
import java.time.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletAdjustRequestedEvent {
  private String operationId;     // e.g. paymentId+":wallet:debit"
  private String phone;
  private String type;            // "recibir" | "gastar"
  private BigDecimal amount;
  private String reason;
  private String traceId;
  private Instant requestedAt;
}