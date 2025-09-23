package com.nttdata.walletpaymentsservice.kafka;

import com.nttdata.walletpaymentsservice.cache.*;
import com.nttdata.walletpaymentsservice.kafka.events.wallet.WalletCreatedEvent;
import com.nttdata.walletpaymentsservice.kafka.events.wallet.WalletUpdatedEvent;
import com.nttdata.walletpaymentsservice.service.OperationAwaiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.*;
import org.springframework.kafka.annotation.*;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.kafka.support.KafkaHeaders;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletEventsConsumer {

    private final WalletDirectoryCache cache;
    private final OperationAwaiter awaiter;

    @KafkaListener(topics = "${app.topics.wallet-created}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void onWalletCreated(WalletCreatedEvent evt,
                                @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {
        log.info("[WALLET-PAYMENTS] CONSUMED wallet.created | key={} phone={} walletId={}",
                key, evt.getPhone(), evt.getWalletId());

        cache.put(WalletInfo.builder()
                        .walletId(evt.getWalletId())
                        .phone(evt.getPhone())
                        .state(evt.getState())
                        .build()
                )
                .doOnComplete(() -> log.info("[WALLET-PAYMENTS] Cache wallet-created phone={} key={}", evt.getPhone(), key))
                .doOnError(ex -> log.error(" [WALLET-PAYMENTS] Cache wallet-created error", ex))
                .subscribe();
    }

    @KafkaListener(topics = "${app.topics.wallet-updated}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void onWalletUpdated(WalletUpdatedEvent evt,
                                @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key) {

        log.info("[WALLET-PAYMENTS] CONSUMED wallet.updated | key={} phone={} walletId={} linkedCardId={}",
                key, evt.getPhone(), evt.getWalletId(), evt.getLinkedCardId());

        cache.get(evt.getPhone())
                .defaultIfEmpty(WalletInfo.builder().phone(evt.getPhone()).build())
                .flatMapCompletable(info -> {
                    if (evt.getState() != null) info.setState(evt.getState());
                    if (evt.getLinkedCardId() != null) info.setLinkedCardId(evt.getLinkedCardId());
                    if (info.getWalletId() == null && evt.getWalletId() != null) info.setWalletId(evt.getWalletId());

                    return cache.put(info);
                })
                .doOnComplete(() -> log.info("[WALLET-PAYMENTS] Cached wallet-updated phone={} key={}", evt.getPhone(), key))
                .doOnError(ex -> log.error("[WALLET-PAYMENTS] Cache wallet-updated error", ex))
                .subscribe();
    }

}
