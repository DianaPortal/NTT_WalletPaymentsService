package com.nttdata.WalletPaymentsService.domain;


import com.nttdata.walletpaymentsservice.model.*;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.*;

import java.math.*;
import java.time.*;

@Data
@Builder
@Document(collection = "payments")
@CompoundIndexes({
    @CompoundIndex(name = "idx_from_created", def = "{'fromPhone': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "idx_to_created",   def = "{'toPhone': 1,   'createdAt': -1}")
})
public class Payment {
  @Id
  private String id;

  /**
   * Idempotencia: único por combinación de petición
   */
  @Indexed(unique = true)
  private String requestId;

  private String fromPhone;
  private String toPhone;
  private BigDecimal amount;

  private PaymentStatus status;
  private String failureReason;   // INS UFFICIENT_FUNDS | WALLET_NOT_FOUND | WALLET_BLOCKED | ...

  private Instant createdAt;
  private Instant completedAt;

  private String traceId;
}
