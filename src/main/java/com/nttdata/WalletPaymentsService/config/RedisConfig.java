package com.nttdata.walletpaymentsservice.config;

import com.fasterxml.jackson.databind.*;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.lettuce.*;
import org.springframework.data.redis.core.*;

@Configuration
public class RedisConfig {
  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(); // host/puerto via properties
  }

  @Bean
  public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory cf) {
    return new StringRedisTemplate(cf);
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper().findAndRegisterModules();
  }
}
