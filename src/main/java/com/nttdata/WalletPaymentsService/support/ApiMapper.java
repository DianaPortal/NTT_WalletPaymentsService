package com.nttdata.WalletPaymentsService.support;

import com.nttdata.WalletPaymentsService.domain.Payment;
import com.nttdata.walletpaymentsservice.model.*;
import org.springframework.stereotype.*;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.*;

@Component
public class ApiMapper {
  public PaymentResource toResource(Payment p) {
    PaymentResource r = new PaymentResource();
    r.setId(p.getId());
    r.setFromPhone(p.getFromPhone());
    r.setToPhone(p.getToPhone());
    r.setAmount(p.getAmount().doubleValue());
    r.setStatus(PaymentStatus.valueOf(p.getStatus().name()));
    r.setFailureReason(JsonNullable.of(p.getFailureReason()));
    r.setCreatedAt(OffsetDateTime.ofInstant(p.getCreatedAt(), ZoneOffset.UTC));
    if (p.getCompletedAt() != null) {
      r.setCompletedAt(JsonNullable.of(OffsetDateTime.ofInstant(p.getCompletedAt(), ZoneOffset.UTC)));
    }
    r.setTraceId(JsonNullable.of(p.getTraceId()));
    return r;
  }
}
