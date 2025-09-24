package com.nttdata.WalletPaymentsService.kafka.events.payment;

import lombok.*;

import java.math.*;
import java.time.*;

@Data
@Builder
public class PaymentFailedEvent {
  private String eventId;
  private String paymentId;
  private String fromPhone;
  private String toPhone;
  private BigDecimal amount;
  private String reason;      // e.g. WALLET_NOT_FOUND | WALLET_BLOCKED | ...
  private Instant occurredAt;
  private String traceId;
}
