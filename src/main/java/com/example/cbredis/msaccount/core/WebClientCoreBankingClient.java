package com.example.cbredis.msaccount.core;

import com.example.cbredis.msaccount.domain.AccountBalance;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

@Component
public class WebClientCoreBankingClient implements CoreBankingClient{

    //core-banking-service - GET http://core-banking-service/accounts/core/{accountId}/balance
    private final WebClient webClient;

    public WebClientCoreBankingClient(WebClient.Builder builder,
                                      @Value("${core-banking.base-url}") String baseUrl)
    {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<AccountBalance> getAccountBalance(String accountId) {

        System.out.println(" -> Llamando a Core Bancario");
        return webClient
                .get()
                .uri("/accounts/core/{accountId}/balance", accountId)
                .retrieve()
                .bodyToMono(CoreBankingBalanceResponse.class)
                .map(r -> new AccountBalance(
                        r.accountId(),
                        r.balance(),
                        r.currency(),
                        r.accountType()
                ));
    }

    @Override
    public Mono<AccountBalance> getAccountBalanceCbRetry(String accountId) {

        System.out.println(" -> Llamando a Core Bancario");
        return webClient
                .get()
                .uri("/accounts/core/{accountId}/balance", accountId)
                .retrieve()
                .bodyToMono(CoreBankingBalanceResponse.class)
                .map(r -> new AccountBalance(
                        r.accountId(),
                        r.balance(),
                        r.currency(),
                        r.accountType()
                ));
    }

    @Override
    @RateLimiter(name = "backendService", fallbackMethod = "fallbackRateLimiter")
    public Mono<AccountBalance> getAccountBalanceRl(String accountId) {

        System.out.println(" -> Llamando a Core Bancario");
        return webClient
                .get()
                .uri("/accounts/core/{accountId}/balance", accountId)
                .retrieve()
                .bodyToMono(CoreBankingBalanceResponse.class)
                .map(r -> new AccountBalance(
                        r.accountId(),
                        r.balance(),
                        r.currency(),
                        r.accountType()
                ));
    }

    @Override
    public Mono<AccountBalance> getAccountBalanceTl(String accountId) {

        System.out.println(" -> Llamando a Core Bancario");
        return webClient
                .get()
                .uri("/accounts/core/{accountId}/balance", accountId)
                .retrieve()
                .bodyToMono(CoreBankingBalanceResponse.class)
                .map(r -> new AccountBalance(
                        r.accountId(),
                        r.balance(),
                        r.currency(),
                        r.accountType()
                ));
    }

    private Mono<AccountBalance> fallbackRateLimiter(String accountId, Throwable throwable) {
        System.out.println("Ingreso a Fallback RateLimiter");
        return Mono.just(new AccountBalance(
                accountId,
                BigDecimal.ZERO,
                "ERROR RATELIMITER",
                "ERROR RATELIMITER"
        ));
    }

}
