package com.example.cbredis.msaccount.fee;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component("percentageFeeStrategy")
public class PercentageFeeStrategy implements FeeStrategy {

    @Override
    public BigDecimal calculateFee(BigDecimal balance) {
        return balance.multiply(new BigDecimal("0.05"));
    }

}