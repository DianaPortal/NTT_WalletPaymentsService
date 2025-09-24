package com.nttdata.WalletPaymentsService.api;

import com.nttdata.WalletPaymentsService.service.PaymentService;
import com.nttdata.WalletPaymentsService.support.ApiMapper;
import com.nttdata.walletpaymentsservice.api.PaymentsApiDelegate;
import com.nttdata.walletpaymentsservice.model.*;
import lombok.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import java.util.stream.Collectors;
import java.util.*;

@Component
@RequiredArgsConstructor
public class PaymentsApiDelegateImpl implements PaymentsApiDelegate {
  @Qualifier("paymentServiceImpl")
  private final PaymentService service;
  private final ApiMapper mapper;

  @Override
  public ResponseEntity<PaymentResource> createPayment(PaymentRequest paymentRequest, String idempotencyKey) {
    String traceId = getOrCreateTraceId();
    var saved = service.createPayment(paymentRequest, idempotencyKey, traceId).blockingGet();
    var body = mapper.toResource(saved);
    return ResponseEntity.accepted().header("X-Trace-Id", traceId).body(body);
  }

  @Override
  public ResponseEntity<PaymentResource> getPaymentById(String paymentId) {
    var p = service.getPayment(paymentId).blockingGet();
    var body = mapper.toResource(p);
    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<InlineResponse200> health() {
    InlineResponse200 response = new InlineResponse200();
    response.setStatus("UP");
    return ResponseEntity.ok(response);
  }

  private String getOrCreateTraceId() {
    String current = MDC.get("traceId");
    if (current == null || current.isEmpty()) {
      current = UUID.randomUUID().toString();
      MDC.put("traceId", current);
    }
    return current;
  }


  @Override
  public ResponseEntity<List<PaymentResource>> listSentByPhone(String phone) {
    var list = service.listSentByPhone(phone).blockingGet();
    var body = list.stream().map(mapper::toResource).collect(Collectors.toList());
    return ResponseEntity.ok(body);
  }

  @Override
  public ResponseEntity<List<PaymentResource>> listReceivedByPhone(String phone) {
    var list = service.listReceivedByPhone(phone).blockingGet();
    var body = list.stream().map(mapper::toResource).collect(Collectors.toList());
    return ResponseEntity.ok(body);
  }

}
