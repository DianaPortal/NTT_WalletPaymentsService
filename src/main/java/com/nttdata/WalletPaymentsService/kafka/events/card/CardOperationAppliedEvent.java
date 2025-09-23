package com.nttdata.walletpaymentsservice.kafka.events.card;

import lombok.*;

import java.math.*;
import java.time.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardOperationAppliedEvent {
  private String operationId;
  private String cardId;
  private String type;            // "debit" | "credit"
  private BigDecimal amount;
  private String traceId;
  private Instant processedAt;
}