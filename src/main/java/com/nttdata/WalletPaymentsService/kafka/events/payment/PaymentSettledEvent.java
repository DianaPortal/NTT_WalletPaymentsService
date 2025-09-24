package com.nttdata.WalletPaymentsService.kafka.events.payment;

import lombok.*;

import java.math.*;
import java.time.*;

@Data
@Builder
public class PaymentSettledEvent {
  private String eventId;
  private String paymentId;
  private String fromPhone;
  private String toPhone;
  private BigDecimal amount;
  private Instant occurredAt;
  private String traceId;
}
