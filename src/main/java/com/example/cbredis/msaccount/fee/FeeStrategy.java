package com.example.cbredis.msaccount.fee;

import java.math.BigDecimal;

public interface FeeStrategy {
    BigDecimal calculateFee(BigDecimal balance);
}
