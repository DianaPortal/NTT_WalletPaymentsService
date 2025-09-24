package com.nttdata.WalletPaymentsService.service.impl;
import com.nttdata.WalletPaymentsService.cache.WalletDirectoryCache;
import com.nttdata.WalletPaymentsService.cache.WalletInfo;
import com.nttdata.WalletPaymentsService.domain.Payment;
import com.nttdata.WalletPaymentsService.kafka.BankCommandProducer;
import com.nttdata.WalletPaymentsService.kafka.PaymentEventsProducer;
import com.nttdata.WalletPaymentsService.kafka.WalletCommandProducer;
import com.nttdata.WalletPaymentsService.kafka.events.card.CardDebitRequestedEvent;
import com.nttdata.WalletPaymentsService.kafka.events.card.CardDebitTransferInRequestedEvent;
import com.nttdata.WalletPaymentsService.kafka.events.payment.PaymentFailedEvent;
import com.nttdata.WalletPaymentsService.kafka.events.payment.PaymentSettledEvent;
import com.nttdata.WalletPaymentsService.kafka.events.wallet.WalletAdjustRequestedEvent;
import com.nttdata.WalletPaymentsService.repository.PaymentRepository;
import com.nttdata.WalletPaymentsService.service.OperationAwaiter;
import com.nttdata.WalletPaymentsService.service.PaymentService;

import com.nttdata.WalletPaymentsService.support.BusinessException;
import com.nttdata.WalletPaymentsService.support.ErrorCodes;
import com.nttdata.WalletPaymentsService.util.PhoneUtils;
import com.nttdata.walletpaymentsservice.model.PaymentStatus;
import com.nttdata.walletpaymentsservice.model.PaymentRequest;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.openapitools.jackson.nullable.JsonNullable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import java.util.List;
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
  private final PaymentRepository payments;
  private final WalletDirectoryCache cache;
  private final PaymentEventsProducer producer;
  private final BankCommandProducer bank;
  private final WalletCommandProducer wallet;
  private final OperationAwaiter awaiter;

  @Override
  public Single<Payment> createPayment(PaymentRequest req, String idempotencyKey, String traceId) {
    validateInput(req);

    // 🔹 Normalizar teléfonos (quita +51 si viene)
    String fromPhone = PhoneUtils.normalizePhone(req.getFromPhone());
    String toPhone   = PhoneUtils.normalizePhone(req.getToPhone());

    // 🔹 Construcción de requestId con los phones normalizados
    final String requestId = (idempotencyKey != null && !idempotencyKey.isEmpty())
        ? idempotencyKey
        : UUID.nameUUIDFromBytes(
        (fromPhone + "|" + toPhone + "|" + req.getAmount() + "|" +
            Optional.ofNullable(req.getMessage()).orElse(JsonNullable.of("")).orElse(""))
            .getBytes()
    ).toString();

    // 1) Idempotencia
    return Single.fromCallable(() -> payments.findByRequestId(requestId))
        .subscribeOn(Schedulers.io())
        .flatMap(opt -> opt.map(Single::just).orElseGet(() ->
            // 2) Validaciones de wallets desde Redis (phones normalizados)
            Single.zip(
                    cache.get(fromPhone).switchIfEmpty(Maybe.error(new BusinessException(
                        ErrorCodes.WALLET_NOT_FOUND, "fromPhone wallet not found", 404))).toSingle(),
                    cache.get(toPhone).switchIfEmpty(Maybe.error(new BusinessException(
                        ErrorCodes.WALLET_NOT_FOUND, "toPhone wallet not found", 404))).toSingle(),
                    (from, to) -> validateBusiness(from, to, BigDecimal.valueOf(req.getAmount()))
                )
                // 3) Persistir estado REQUESTED
                .flatMap(v -> Single.fromCallable(() -> payments.save(Payment.builder()
                        .requestId(requestId)
                        .fromPhone(fromPhone)   // <-- guardamos normalizado
                        .toPhone(toPhone)       // <-- guardamos normalizado
                        .amount(BigDecimal.valueOf(req.getAmount()))
                        .status(PaymentStatus.REQUESTED)
                        .createdAt(Instant.now())
                        .traceId(traceId)
                        .build()))
                    .subscribeOn(Schedulers.io()))
                // 4) Disparar procesamiento asíncrono (settle/fail) y devolver 202 con REQUESTED
                .doOnSuccess(saved -> processAsync(saved).subscribe(() -> {}, ex -> {}))
        ));
  }

  @Override
  public Maybe<Payment> getPayment(String id) {
    return Maybe.fromCallable(() -> payments.findById(id)).subscribeOn(Schedulers.io())
        .flatMap(opt -> opt.map(Maybe::just).orElseGet(Maybe::empty));
  }

  @Override
  public Single<List<Payment>> listSentByPhone(String phone) {
    return Single
        .fromCallable(() -> payments.findByFromPhoneOrderByCreatedAtDesc(phone))
        .subscribeOn(Schedulers.io());
  }

  @Override
  public Single<List<Payment>> listReceivedByPhone(String phone) {
    return Single
        .fromCallable(() -> payments.findByToPhoneOrderByCreatedAtDesc(phone))
        .subscribeOn(Schedulers.io());
  }


  // ------- helpers -------

  private void validateInput(PaymentRequest req) {
    if (req.getAmount() == null || req.getAmount() <= 0) {
      throw new BusinessException(ErrorCodes.VALIDATION_ERROR, "amount must be > 0", 400);
    }
    if (req.getFromPhone().equals(req.getToPhone())) {
      throw new BusinessException(ErrorCodes.SAME_WALLET, "fromPhone and toPhone cannot be the same", 422);
    }
  }

  private boolean validateBusiness(WalletInfo from, WalletInfo to, BigDecimal amount) {
    if (!"ACTIVE".equalsIgnoreCase(from.getState())) {
      throw new BusinessException(ErrorCodes.WALLET_BLOCKED, "from wallet not ACTIVE", 422);
    }
    if (!"ACTIVE".equalsIgnoreCase(to.getState())) {
      throw new BusinessException(ErrorCodes.WALLET_BLOCKED, "to wallet not ACTIVE", 422);
    }
    // Si existiese verificación de fondos, hacerlo aquí (contra cuenta asociada, etc.)
    return true;
  }

  private Completable processAsync(Payment p) {
    final String traceId = p.getTraceId();
    final String paymentId = p.getId();

      return Maybe.zip(
                      cache.get(p.getFromPhone())
                              .switchIfEmpty(Maybe.error(new BusinessException(
                                      ErrorCodes.WALLET_NOT_FOUND, "fromPhone wallet not found", 404))),
                      cache.get(p.getToPhone())
                              .switchIfEmpty(Maybe.error(new BusinessException(
                                      ErrorCodes.WALLET_NOT_FOUND, "toPhone wallet not found", 404))),
                      (from, to) -> new WalletInfo[]{from, to}
              )
              .toSingle()
              .flatMapCompletable(arr -> {
                  WalletInfo from = arr[0], to = arr[1];

                  // ---- Paso 1: DEBIT del emisor ----
                  String opDebit;
                  Completable emitDebit;
                  if (hasText(from.getLinkedCardId())) {
                      opDebit = paymentId + ":card:debit";
                      emitDebit = bank.requestDebit(CardDebitRequestedEvent.builder()
                              .operationId(opDebit)
                              .cardId(from.getLinkedCardId())
                              .amount(p.getAmount())
                              .traceId(traceId)
                              .requestedAt(Instant.now())
                              .build());
                  } else {
                      opDebit = paymentId + ":wallet:debit";
                      emitDebit = wallet.requestAdjust(WalletAdjustRequestedEvent.builder()
                              .operationId(opDebit)
                              .phone(p.getFromPhone())
                              .type("debit")                  // si no usas type, bórralo
                              .amount(p.getAmount())
                              .traceId(traceId)               // o correlationId
                              .requestedAt(Instant.now())
                              .build());
                  }

                  // ---- Paso 2: CREDIT del receptor ----
                  String opCreditCard   = paymentId + ":card:credit";
                  String opCreditWallet = paymentId + ":wallet:credit";

                  Completable emitCreditCard = bank.requestCredit(CardDebitTransferInRequestedEvent.builder()
                          .operationId(opCreditCard)
                          .cardId(to.getLinkedCardId())
                          .amount(p.getAmount())
                          .traceId(traceId)
                          .requestedAt(Instant.now())
                          .build());

                  Completable emitCreditWallet = wallet.requestAdjust(WalletAdjustRequestedEvent.builder()
                          .operationId(opCreditWallet)
                          .phone(p.getToPhone())
                          .type("credit")                 // si no usas type, bórralo
                          .amount(p.getAmount())
                          .traceId(traceId)
                          .requestedAt(Instant.now())
                          .build());

                  // Cadena SAGA: debit -> await -> credit -> await
                  Completable creditFlow = hasText(to.getLinkedCardId())
                          ? emitCreditCard
                          .andThen(awaiter.await(opCreditCard)
                                  .onErrorResumeNext(ex -> Completable.error(new IllegalStateException("CREDIT_DENIED"))))
                          : emitCreditWallet
                          .andThen(awaiter.await(opCreditWallet)
                                  .onErrorResumeNext(ex -> Completable.error(new IllegalStateException("CREDIT_DENIED"))));

                  return emitDebit
                          .andThen(awaiter.await(opDebit)
                                  .onErrorResumeNext(ex -> Completable.error(new IllegalStateException("DEBIT_DENIED"))))
                          .andThen(creditFlow)
                          // ---- Éxito total: guardar y publicar settled (sin bloquear) ----
                          .andThen(Completable.fromCallable(() -> {
                              p.setStatus(PaymentStatus.COMPLETED);
                              p.setCompletedAt(Instant.now());
                              payments.save(p); // repo bloqueante
                              return true;
                          }).subscribeOn(Schedulers.io()))
                          .andThen(producer.sendSettled(PaymentSettledEvent.builder()
                                  .eventId(UUID.randomUUID().toString())
                                  .paymentId(p.getId())
                                  .fromPhone(p.getFromPhone())
                                  .toPhone(p.getToPhone())
                                  .amount(p.getAmount())
                                  .occurredAt(Instant.now())
                                  .traceId(traceId)
                                  .build()));
              })
              // ---- Falla en cualquier paso: guardar y publicar failed (sin bloquear) ----
              .onErrorResumeNext(ex ->
                      Completable.fromCallable(() -> {
                                  p.setStatus(PaymentStatus.FAILED);
                                  p.setFailureReason(ex.getMessage());
                                  p.setCompletedAt(Instant.now());
                                  payments.save(p);
                                  return true;
                              }).subscribeOn(Schedulers.io())
                              .andThen(producer.sendFailed(PaymentFailedEvent.builder()
                                      .eventId(UUID.randomUUID().toString())
                                      .paymentId(p.getId())
                                      .fromPhone(p.getFromPhone())
                                      .toPhone(p.getToPhone())
                                      .amount(p.getAmount())
                                      .reason(p.getFailureReason())
                                      .occurredAt(Instant.now())
                                      .traceId(traceId)
                                      .build()))
              )
              .subscribeOn(Schedulers.io());
  }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

}
