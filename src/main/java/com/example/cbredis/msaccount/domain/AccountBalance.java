package com.example.cbredis.msaccount.domain;

import java.math.BigDecimal;

public record AccountBalance(String accountId,
                             BigDecimal balance,
                             String currency,
                             String accountType) {
}

