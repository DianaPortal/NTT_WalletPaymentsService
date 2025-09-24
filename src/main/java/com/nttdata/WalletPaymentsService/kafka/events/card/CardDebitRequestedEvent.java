package com.nttdata.WalletPaymentsService.kafka.events.card;

import lombok.*;

import java.math.*;
import java.time.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardDebitRequestedEvent {
  private String operationId;     // paymentId+":card:debit"
  private String cardId;
  private BigDecimal amount;
  private String traceId;
  private Instant requestedAt;
}
