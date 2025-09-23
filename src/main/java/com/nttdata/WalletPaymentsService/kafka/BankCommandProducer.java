package com.nttdata.walletpaymentsservice.kafka;

import com.nttdata.walletpaymentsservice.kafka.events.card.CardDebitRequestedEvent;
import com.nttdata.walletpaymentsservice.kafka.events.card.CardDebitTransferInRequestedEvent;
import io.reactivex.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.*;
import org.springframework.stereotype.*;

@Component
@RequiredArgsConstructor
public class BankCommandProducer {

  private final KafkaTemplate<String, Object> kafka;

  @Value("${app.topics.card-debit-req}")
  private String debitReqTopic;
  @Value("${app.topics.card-credit-req}")
  private String creditReqTopic;

  public Completable requestDebit(CardDebitRequestedEvent evt) {
    return Completable.fromFuture(kafka.send(debitReqTopic, evt.getCardId(), evt).completable());
  }

  public Completable requestCredit(CardDebitTransferInRequestedEvent evt) {
    return Completable.fromFuture(kafka.send(creditReqTopic, evt.getCardId(), evt).completable());
  }
}
