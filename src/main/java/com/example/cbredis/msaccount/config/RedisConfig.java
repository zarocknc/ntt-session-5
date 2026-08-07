package com.example.cbredis.msaccount.config;

import com.example.cbredis.msaccount.domain.AccountBalance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, AccountBalance> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {

        Jackson2JsonRedisSerializer<AccountBalance> serializer =
                new Jackson2JsonRedisSerializer<>(AccountBalance.class);

        RedisSerializationContext<String, AccountBalance> context = RedisSerializationContext
                .<String, AccountBalance>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}