package com.nttdata.walletpaymentsservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CustomJwtDecoder implements JwtDecoder {

  private final List<String> allowedIssuers;

  public CustomJwtDecoder(@Value("${app.auth.allowed-issuers}") String issuers) {
    this.allowedIssuers = Arrays.asList(issuers.split(","));
  }

  @Override
  public Jwt decode(String token) throws JwtException {
    return tryDecodeWithIssuers(token, 0);
  }

  private Jwt tryDecodeWithIssuers(String token, int index) throws JwtException {
    if (index >= allowedIssuers.size()) {
      throw new JwtValidationException("No se encontró un emisor válido para el token", null);
    }

    String issuer = allowedIssuers.get(index);
    JwtDecoder decoder = NimbusJwtDecoder
        .withJwkSetUri(issuer + "/protocol/openid-connect/certs")
        .build();

    try {
      return decoder.decode(token);
    } catch (JwtException e) {
      // Intentar con el siguiente issuer
      return tryDecodeWithIssuers(token, index + 1);
    }
  }
}