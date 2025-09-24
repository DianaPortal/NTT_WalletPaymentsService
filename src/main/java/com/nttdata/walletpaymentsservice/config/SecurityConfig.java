package com.nttdata.WalletPaymentsService.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.core.convert.converter.*;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final CustomJwtDecoder customJwtDecoder;

  public SecurityConfig(CustomJwtDecoder customJwtDecoder) {
    this.customJwtDecoder = customJwtDecoder;
  }

  @Bean
  public SecurityFilterChain security(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .authorizeRequests(authz -> authz
            .antMatchers("/actuator/**").permitAll()
            .anyRequest().hasAuthority("SCOPE_Partners"))
        .oauth2ResourceServer(oauth -> oauth
            .jwt(jwt -> jwt
                .decoder(customJwtDecoder)
                .jwtAuthenticationConverter(scpAndRoles())));
    return http.build();
  }

  @Bean
  public Converter<Jwt, ? extends AbstractAuthenticationToken> scpAndRoles() {
    JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
    conv.setJwtGrantedAuthoritiesConverter(jwt -> {
      Collection<GrantedAuthority> out = new ArrayList<>();

      Object scope = jwt.getClaims().get("scope");
      if (scope instanceof String) {
        for (String it : ((String) scope).split(" ")) {
          if (!it.isEmpty()) out.add(new SimpleGrantedAuthority("SCOPE_" + it));
        }
      }

      Object scp = jwt.getClaims().get("scp");
      if (scp instanceof Collection) {
        for (Object v : (Collection<?>) scp) {
          if (v != null) out.add(new SimpleGrantedAuthority("SCOPE_" + v));
        }
      }

      Map<?, ?> realm = (Map<?, ?>) jwt.getClaims().get("realm_access");
      if (realm == null) realm = Collections.emptyMap();
      Collection<?> roles = (Collection<?>) realm.get("roles");
      if (roles == null) roles = Collections.emptyList();
      for (Object r : roles) {
        if (r != null) out.add(new SimpleGrantedAuthority("SCOPE_" + r));
      }

      return out;
    });
    return conv;
  }
}