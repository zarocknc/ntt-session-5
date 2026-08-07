package com.example.cbredis.msaccount.cache;

import com.example.cbredis.msaccount.domain.AccountBalance;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AccountBalanceCacheRepository {

    private static final String KEY_PREFIX = "account-balance:";

    private final ReactiveRedisTemplate<String, AccountBalance> redisTemplate;

    @Value("${cache.ttl.account-balance}")
    private Duration ttl;

    public Mono<AccountBalance> findByAccountId(String accountId) {
        String key = KEY_PREFIX + accountId;
        return redisTemplate.opsForValue().get(key);
    }

    public Mono<AccountBalance> save(AccountBalance balance) {
        String key = KEY_PREFIX + balance.accountId();
        return redisTemplate.opsForValue()
                .set(key, balance, ttl)
                .thenReturn(balance);
    }
}