package com.nttdata.walletpaymentsservice.kafka;

import com.nttdata.walletpaymentsservice.kafka.events.wallet.WalletAdjustAppliedEvent;
import com.nttdata.walletpaymentsservice.kafka.events.wallet.WalletAdjustFailedEvent;
import com.nttdata.walletpaymentsservice.service.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.kafka.annotation.*;
import org.springframework.stereotype.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletAdjustResultConsumer {
  private final OperationAwaiter awaiter;

  @KafkaListener(topics = "${app.topics.wallet-adjust-applied}", groupId = "wallet-payments-group")
  public void onApplied(WalletAdjustAppliedEvent evt) {
      log.info("[PAYMENT] Recibido WalletAdjustAppliedEvent | operationId={} | phone={}",
              evt.getOperationId(), evt.getPhone());

    awaiter.complete(evt.getOperationId(), true);

    log.info("[PAYMENT] Operación completada con éxito | operationId={}", evt.getOperationId());

  }

  @KafkaListener(topics = "${app.topics.wallet-adjust-failed}", groupId = "wallet-payments-group")
  public void onFailed(WalletAdjustFailedEvent evt) {
      log.warn("[KAFKA] Recibido WalletAdjustFailedEvent | operationId={} phone={} | reason={}",
              evt.getOperationId(), evt.getPhone(), evt.getReason());

      awaiter.complete(evt.getOperationId(), false);
      log.warn("[PAYMENT] Operación marcada como fallida | operationId={}", evt.getOperationId());

  }
}

