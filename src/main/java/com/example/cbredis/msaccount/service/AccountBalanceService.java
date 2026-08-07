package com.example.cbredis.msaccount.service;

import com.example.cbredis.msaccount.api.AccountBalanceResponse;
import reactor.core.publisher.Mono;

public interface AccountBalanceService {
    public Mono<AccountBalanceResponse> getBalanceWithFee(String accountId);
    public Mono<AccountBalanceResponse> getBalanceCbRetry(String accountId);
    public Mono<AccountBalanceResponse> getBalanceRl(String accountId);
    public Mono<AccountBalanceResponse> getBalanceTl(String accountId);
}
