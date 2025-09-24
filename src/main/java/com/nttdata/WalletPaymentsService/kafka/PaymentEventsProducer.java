package com.nttdata.WalletPaymentsService.kafka;


import com.nttdata.WalletPaymentsService.kafka.events.payment.PaymentFailedEvent;
import com.nttdata.WalletPaymentsService.kafka.events.payment.PaymentSettledEvent;
import io.reactivex.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.kafka.core.*;
import org.springframework.stereotype.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventsProducer {
  private final KafkaTemplate<String, Object> kafka;

  @Value("${app.topics.payment-settled}") String topicSettled;
  @Value("${app.topics.payment-failed}")  String topicFailed;

  public Completable sendSettled(PaymentSettledEvent evt) {
      log.info("[PAYMENT] Enviando PaymentSettledEvent al topic={} | fromPhone={}", topicSettled, evt.getFromPhone());
      return Completable.fromFuture(
              kafka.send(topicSettled, evt.getFromPhone(), evt)
                      .completable()
      ).doOnComplete(() ->
              log.info("[PAYMENT] PaymentSettledEvent publicado correctamente | fromPhone={}", evt.getFromPhone())
      ).doOnError(err ->
              log.error("[PAYMENT] Error publicando PaymentSettledEvent | fromPhone={} | error={}", evt.getFromPhone(), err.getMessage(), err)
      );
  }
  public Completable sendFailed(PaymentFailedEvent evt) {
      log.info("[PAYMENT] Enviando PaymentFailedEvent al topic={} | fromPhone={}", topicFailed, evt.getFromPhone());
      return Completable.fromFuture(
              kafka.send(topicFailed, evt.getFromPhone(), evt)
                      .completable()
       ).doOnComplete(() ->
              log.info("[PAYMENT] PaymentFailedEvent publicado correctamente | fromPhone={}", evt.getFromPhone())
      ).doOnError(err ->
              log.error("[PAYMENT] Error publicando PaymentFailedEvent | fromPhone={} | error={}", evt.getFromPhone(), err.getMessage(), err)
      );
  }

}
//  @Value("${app.topics.payment-settled:wallet.payment-settled}")
//  private String topicPaymentSettled;
//
//  @Value("${app.topics.payment-failed:wallet.payment-failed}")
//  private String topicPaymentFailed;
//
//  public PaymentEventsProducer(KafkaTemplate<String, Object> kafkaTemplate) {
//    this.kafkaTemplate = kafkaTemplate;
//  }
//
//  public Completable sendSettled(PaymentSettledEvent evt) {
//    return Completable.fromFuture(kafkaTemplate.send(topicPaymentSettled, evt.getFromPhone(), evt).completable());
//  }
//
//  public Completable sendFailed(PaymentFailedEvent evt) {
//    return Completable.fromFuture(kafkaTemplate.send(topicPaymentFailed, evt.getFromPhone(), evt).completable());
//  }

