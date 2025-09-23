package com.nttdata.walletpaymentsservice.kafka.events.wallet;

import lombok.*;

import java.time.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletAdjustFailedEvent {
  private String operationId;
  private String phone;
  private String reason;
  private String traceId;
  private Instant processedAt;
}
