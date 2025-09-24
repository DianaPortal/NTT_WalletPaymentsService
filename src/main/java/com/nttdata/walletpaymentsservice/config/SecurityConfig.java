package com.nttdata.walletpaymentsservice.config;

import org.springframework.context.annotation.*;
import org.springframework.core.convert.converter.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.*;

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