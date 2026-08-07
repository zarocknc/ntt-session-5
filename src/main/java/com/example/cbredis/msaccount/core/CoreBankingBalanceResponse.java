package com.example.cbredis.msaccount.core;

import java.math.BigDecimal;

public record CoreBankingBalanceResponse(String accountId,
                                         BigDecimal balance,
                                         String currency,
                                         String accountType) {
}

