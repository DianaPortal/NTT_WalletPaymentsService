package com.nttdata.WalletPaymentsService.kafka.events.card;

import lombok.*;

import java.time.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardOperationDeniedEvent {
  private String operationId;
  private String cardId;
  private String type;            // "debit" | "credit"
  private String reason;
  private String traceId;
  private Instant processedAt;
}
