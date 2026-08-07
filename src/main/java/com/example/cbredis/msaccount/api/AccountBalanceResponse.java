package com.example.cbredis.msaccount.api;

import java.math.BigDecimal;

public record AccountBalanceResponse(String accountId,
                                     BigDecimal balance,
                                     String currency,
                                     BigDecimal feeApplied,
                                     String accountType) {
}

