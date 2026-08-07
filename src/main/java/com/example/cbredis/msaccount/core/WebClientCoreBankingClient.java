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
import java.util.concurrent.CompletableFuture;

@Component
public class WebClientCoreBankingClient implements CoreBankingClient {

    //core-banking-service - GET http://core-banking-service/accounts/core/{accountId}/balance
    private final WebClient webClient;

    public WebClientCoreBankingClient(WebClient.Builder builder,
                                       @Value("${core-banking.base-url}") String baseUrl) {
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
                .map(this::toAccountBalance);
    }

    @Override
    @CircuitBreaker(name = "coreBankingCbRetry", fallbackMethod = "fallbackBalance")
    @Retry(name = "coreBankingCbRetry")
    public Mono<AccountBalance> getAccountBalanceCbRetry(String accountId) {
        System.out.println(" -> Llamando a Core Bancario (CbRetry)");
        return webClient
                .get()
                .uri("/accounts/core/{accountId}/balance", accountId)
                .retrieve()
                .bodyToMono(CoreBankingBalanceResponse.class)
                .map(this::toAccountBalance);
    }

    @Override
    @RateLimiter(name = "backendService", fallbackMethod = "fallbackRateLimiter")
    public Mono<AccountBalance> getAccountBalanceRl(String accountId) {
        System.out.println(" -> Llamando a Core Bancario (Rl)");
        return webClient
                .get()
                .uri("/accounts/core/{accountId}/balance", accountId)
                .retrieve()
                .bodyToMono(CoreBankingBalanceResponse.class)
                .map(this::toAccountBalance);
    }

    @Override
    @TimeLimiter(name = "coreBankingTl", fallbackMethod = "fallbackBalanceTimeLimiter")
    public CompletableFuture<AccountBalance> getAccountBalanceTl(String accountId) {
        System.out.println(" -> Llamando a Core Bancario (Tl)");
        return webClient
                .get()
                .uri("/accounts/core/{accountId}/balance", accountId)
                .retrieve()
                .bodyToMono(CoreBankingBalanceResponse.class)
                .map(this::toAccountBalance)
                .toFuture();
    }

    private AccountBalance toAccountBalance(CoreBankingBalanceResponse r) {
        return new AccountBalance(
                r.accountId(),
                r.balance(),
                r.currency(),
                r.accountType()
        );
    }

    private Mono<AccountBalance> fallbackBalance(String accountId, Throwable throwable) {
        System.out.println("Ingreso a Fallback (CbRetry): " + throwable.getMessage());
        return Mono.just(new AccountBalance(
                accountId,
                BigDecimal.ZERO,
                "ERROR",
                "ERROR"
        ));
    }

    private CompletableFuture<AccountBalance> fallbackBalanceTimeLimiter(String accountId, Throwable throwable) {
        System.out.println("Ingreso a Fallback (TimeLimiter): " + throwable.getMessage());
        return CompletableFuture.completedFuture(new AccountBalance(
                accountId,
                BigDecimal.ZERO,
                "ERROR TIMELIMITER",
                "ERROR TIMELIMITER"
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