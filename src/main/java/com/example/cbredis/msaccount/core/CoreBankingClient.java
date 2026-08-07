package com.example.cbredis.msaccount.core;

import com.example.cbredis.msaccount.domain.AccountBalance;
import reactor.core.publisher.Mono;

public interface CoreBankingClient {
    Mono<AccountBalance> getAccountBalance(String accountId);
    Mono<AccountBalance> getAccountBalanceCbRetry(String accountId);
    Mono<AccountBalance> getAccountBalanceRl(String accountId);
    Mono<AccountBalance> getAccountBalanceTl(String accountId);
}
