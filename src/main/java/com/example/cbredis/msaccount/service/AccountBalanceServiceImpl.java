package com.example.cbredis.msaccount.service;

import com.example.cbredis.msaccount.api.AccountBalanceResponse;
import com.example.cbredis.msaccount.core.CoreBankingClient;
import com.example.cbredis.msaccount.domain.AccountBalance;
import com.example.cbredis.msaccount.fee.FeeStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountBalanceServiceImpl implements AccountBalanceService{

    private final AccountBalanceCacheRepository cacheRepository;
    private final CoreBankingClient coreBankingClient;
    private final FeeStrategySelector feeStrategySelector;

    @Override
    public Mono<AccountBalanceResponse> getBalanceWithFee(String accountId) {
        return cacheRepository.findByAccountId(accountId)
                .switchIfEmpty(fetchFromCoreAndCache(accountId))
                .map(this::applyFee);
    }

    private Mono<AccountBalance> fetchFromCoreAndCache(String accountId) {
        return getBalanceFromCoreBanking(accountId)
                .flatMap(balance -> cacheRepository.save(balance)
                        .thenReturn(balance));
    }

    public Mono<AccountBalance> getBalanceFromCoreBanking(String accountId) {
        return coreBankingClient.getAccountBalance(accountId);
    }

    private AccountBalanceResponse applyFee(AccountBalance balance) {
        FeeStrategy strategy = feeStrategySelector.select(balance.accountType());
        BigDecimal fee = strategy.calculateFee(balance.balance());
        return new AccountBalanceResponse(
                balance.accountId(),
                balance.balance(),
                balance.currency(),
                fee,
                balance.accountType()
        );
    }

    @Override
    public Mono<AccountBalanceResponse> getBalanceCbRetry(String accountId) {
        return coreBankingClient.getAccountBalanceCbRetry(accountId)
                .map(balance ->
                    new AccountBalanceResponse(
                            balance.accountId(),
                            balance.balance(),
                            balance.currency(),
                            new BigDecimal(0),
                            balance.accountType())
                );
    }

    @Override
    public Mono<AccountBalanceResponse> getBalanceRl(String accountId) {
        return coreBankingClient.getAccountBalanceRl(accountId)
                .map(balance ->
                        new AccountBalanceResponse(
                                balance.accountId(),
                                balance.balance(),
                                balance.currency(),
                                new BigDecimal(0),
                                balance.accountType())
                );
    }

    @Override
    public Mono<AccountBalanceResponse> getBalanceTl(String accountId) {
        return coreBankingClient.getAccountBalanceTl(accountId)
                .map(balance ->
                        new AccountBalanceResponse(
                                balance.accountId(),
                                balance.balance(),
                                balance.currency(),
                                new BigDecimal(0),
                                balance.accountType())
                );
    }

}
