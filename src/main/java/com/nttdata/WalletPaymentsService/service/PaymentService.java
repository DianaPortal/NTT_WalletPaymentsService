package com.nttdata.WalletPaymentsService.service;

import com.nttdata.WalletPaymentsService.domain.Payment;
import com.nttdata.walletpaymentsservice.model.PaymentRequest;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface  PaymentService {

  Single<Payment> createPayment(PaymentRequest req, String idempotencyKey, String traceId);
  Maybe<Payment> getPayment(String id);
  Single<List<Payment>> listSentByPhone(String phone);

  Single<List<Payment>> listReceivedByPhone(String phone);


}
