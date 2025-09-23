package com.nttdata.walletpaymentsservice.support;

import com.nttdata.walletpaymentsservice.model.*;
import com.nttdata.walletpaymentsservice.model.Error;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Error> handleBusiness(BusinessException ex) {
    Error err = new Error();
    err.setCode(ex.getCode());
    err.setMessage(ex.getMessage());
    return ResponseEntity.status(ex.getHttpStatus()).body(err);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Error> handleGeneric(Exception ex) {
    Error err = new Error();
    err.setCode(ErrorCodes.INTERNAL_ERROR);
    err.setMessage("Unexpected error");
    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(err);
  }
}
