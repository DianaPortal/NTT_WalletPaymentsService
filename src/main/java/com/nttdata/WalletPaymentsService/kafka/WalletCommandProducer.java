package com.nttdata.WalletPaymentsService.kafka;

import com.nttdata.WalletPaymentsService.kafka.events.wallet.WalletAdjustRequestedEvent;
import io.reactivex.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.*;
import org.springframework.stereotype.*;

@Component
@RequiredArgsConstructor
public class WalletCommandProducer {

    private final KafkaTemplate<String, Object> kafka;
    @Value("${app.topics.wallet-adjust-req}") String topic;

    public Completable requestAdjust(WalletAdjustRequestedEvent evt) {
      return Completable.fromFuture(kafka.send(topic, evt.getPhone(), evt).completable());
    }
}
//  private final KafkaTemplate<String, Object> kafka;
//
//  @Value("${app.topics.wallet-adjust-req}")
//  private String adjustReqTopic;
//
//  public Completable requestAdjust(WalletAdjustRequestedEvent evt) {
//    return Completable.fromFuture(kafka.send(adjustReqTopic, evt.getPhone(), evt).completable());
//  }
//}