package com.nttdata.WalletPaymentsService.cache;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import io.reactivex.*;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.*;

import java.time.*;
import java.util.*;

@Component
public class WalletDirectoryCache {
  private static final String KEY_PREFIX = "wallet:by-phone:";
  private final StringRedisTemplate redis;
  private final ObjectMapper om;

  public WalletDirectoryCache(StringRedisTemplate redis, ObjectMapper om) {
    this.redis = redis;
    this.om = om;
  }

  public Maybe<WalletInfo> get(String phone) {
    return Maybe.fromCallable(() -> redis.opsForValue().get(KEY_PREFIX + phone))
        .filter(Objects::nonNull)
        .map(json -> {
          try {
            return om.readValue(json, WalletInfo.class);
          } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
          }
        });
  }

  public Completable put(WalletInfo info) {
    return Completable.fromAction(() -> {
      String json = om.writeValueAsString(info);
      redis.opsForValue().set(KEY_PREFIX + info.getPhone(), json, Duration.ofHours(12));
    });
  }

  public Completable evict(String phone) {
    return Completable.fromAction(() -> redis.delete(KEY_PREFIX + phone));
  }

}
