package com.nttdata.WalletPaymentsService.config;

import org.apache.kafka.clients.admin.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

@Configuration
public class KafkaConfig {
  @Bean
  public NewTopic topicPaymentSettled(@Value("${app.topics.payment-settled:wallet.payment-settled}") String name) {
    return new NewTopic(name, 3, (short) 1);
  }

  @Bean
  public NewTopic topicPaymentFailed(@Value("${app.topics.payment-failed:wallet.payment-failed}") String name) {
    return new NewTopic(name, 3, (short) 1);
  }

  @Bean
  public NewTopic topicWalletAdjustReq(@Value("${app.topics.wallet-adjust-req}") String n) {
    return new NewTopic(n, 3, (short) 1);
  }

  @Bean
  public NewTopic topicWalletAdjustApplied(@Value("${app.topics.wallet-adjust-applied}") String n) {
    return new NewTopic(n, 3, (short) 1);
  }

  @Bean
  public NewTopic topicWalletAdjustFailed(@Value("${app.topics.wallet-adjust-failed}") String n) {
    return new NewTopic(n, 3, (short) 1);
  }

  @Bean
  public NewTopic topicCardDebitReq(@Value("${app.topics.card-debit-req}") String n) {
    return new NewTopic(n, 3, (short) 1);
  }

  @Bean
  public NewTopic topicCardDebitApplied(@Value("${app.topics.card-debit-applied}") String n) {
    return new NewTopic(n, 3, (short) 1);
  }

  @Bean
  public NewTopic topicCardDebitDenied(@Value("${app.topics.card-debit-denied}") String n) {
    return new NewTopic(n, 3, (short) 1);
  }

  @Bean
  public NewTopic topicCardCreditReq(@Value("${app.topics.card-credit-req}") String n) {
    return new NewTopic(n, 3, (short) 1);
  }

  @Bean
  public NewTopic topicCardCreditApplied(@Value("${app.topics.card-credit-applied}") String n) {
    return new NewTopic(n, 3, (short) 1);
  }

  @Bean
  public NewTopic topicCardCreditDenied(@Value("${app.topics.card-credit-denied}") String n) {
    return new NewTopic(n, 3, (short) 1);
  }

}
