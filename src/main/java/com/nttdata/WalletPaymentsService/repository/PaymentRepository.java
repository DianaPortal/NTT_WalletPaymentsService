package com.nttdata.walletpaymentsservice.repository;

import com.nttdata.walletpaymentsservice.domain.*;
import org.springframework.data.mongodb.repository.*;

import java.util.*;

public interface PaymentRepository extends MongoRepository<Payment, String> {
  Optional<Payment> findByRequestId(String requestId);
  List<Payment> findByFromPhoneOrderByCreatedAtDesc(String phone);
  List<Payment> findByToPhoneOrderByCreatedAtDesc(String phone);

}
