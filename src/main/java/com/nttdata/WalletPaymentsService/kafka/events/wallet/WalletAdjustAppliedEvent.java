package com.nttdata.walletpaymentsservice.kafka.events.wallet;

import lombok.*;

import java.math.*;
import java.time.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletAdjustAppliedEvent {
  private String operationId;
  private String phone;
  private BigDecimal newBalance;
  private String traceId;
  private Instant processedAt;
}
