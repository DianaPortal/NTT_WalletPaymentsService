package com.nttdata.WalletPaymentsService.support;

import lombok.*;

@Getter
public class BusinessException extends RuntimeException {
  private final String code;
  private final int httpStatus;

  public BusinessException(String code, String message, int httpStatus) {
    super(message);
    this.code = code;
    this.httpStatus = httpStatus;
  }
}
