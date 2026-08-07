package com.example.cbredis.msaccount.fee;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component("fixedFeeStrategy")
public class FixedFeeStrategy implements FeeStrategy {

    @Override
    public BigDecimal calculateFee(BigDecimal balance) {
        return new BigDecimal("1.00"); // comisión fija
    }

}

