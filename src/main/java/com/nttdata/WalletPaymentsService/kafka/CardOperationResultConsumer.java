package com.nttdata.WalletPaymentsService.kafka;


import com.nttdata.WalletPaymentsService.kafka.events.card.CardOperationAppliedEvent;
import com.nttdata.WalletPaymentsService.kafka.events.card.CardOperationDeniedEvent;
import com.nttdata.WalletPaymentsService.service.OperationAwaiter;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.kafka.annotation.*;
import org.springframework.stereotype.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardOperationResultConsumer {
  private final OperationAwaiter awaiter;

  @KafkaListener(topics = "${app.topics.card-debit-applied}")
  public void debitApplied(CardOperationAppliedEvent evt) {
    awaiter.complete(evt.getOperationId(), true);
  }

  @KafkaListener(topics = "${app.topics.card-debit-denied}")
  public void debitDenied(CardOperationDeniedEvent evt) {
    awaiter.complete(evt.getOperationId(), false);
  }

  @KafkaListener(topics = "${app.topics.card-credit-applied}")
  public void creditApplied(CardOperationAppliedEvent evt) {
    awaiter.complete(evt.getOperationId(), true);
  }

  @KafkaListener(topics = "${app.topics.card-credit-denied}")
  public void creditDenied(CardOperationDeniedEvent evt) {
    awaiter.complete(evt.getOperationId(), false);
  }
}
