package com.example.cbredis.msaccount.api;

import com.example.cbredis.msaccount.service.AccountBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountBalanceController {

    private final AccountBalanceService accountBalanceService;

    @GetMapping("/{accountId}/balance")
    public Mono<AccountBalanceResponse> getBalance(@PathVariable String accountId)
    {
        return accountBalanceService.getBalanceWithFee(accountId);
    }

    @GetMapping("/cbretry/{accountId}/balance")
    public Mono<AccountBalanceResponse> getBalanceCbRetry(@PathVariable String accountId)
    {
        return accountBalanceService.getBalanceCbRetry(accountId);
    }

    @GetMapping("/rl/{accountId}/balance")
    public Mono<AccountBalanceResponse> getBalanceRl(@PathVariable String accountId)
    {
        return accountBalanceService.getBalanceRl(accountId);
    }

    @GetMapping("/tl/{accountId}/balance")
    public Mono<AccountBalanceResponse> getBalanceTl(@PathVariable String accountId)
    {
        return accountBalanceService.getBalanceTl(accountId);
    }

}