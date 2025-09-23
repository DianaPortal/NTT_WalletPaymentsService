package com.nttdata.walletpaymentsservice.util;

public class PhoneUtils {
  public static String normalizePhone(String phone) {
    if (phone == null) return null;
    // si empieza con +51, lo recortamos
    if (phone.startsWith("+51")) {
      return phone.substring(3);
    }
    return phone;
  }
}
