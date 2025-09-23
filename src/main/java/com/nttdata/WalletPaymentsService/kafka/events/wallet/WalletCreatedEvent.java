package com.nttdata.walletpaymentsservice.kafka.events.wallet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletCreatedEvent {
  private String eventId;
  private String walletId;
  private String phone;
  private String state;
  private Instant occurredAt;
  private String traceId;

}
