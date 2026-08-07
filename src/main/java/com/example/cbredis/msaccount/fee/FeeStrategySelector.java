package com.example.cbredis.msaccount.fee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FeeStrategySelector {

    private final Map<String, FeeStrategy> strategies;

    public FeeStrategy select(String accountType) {
        if ("PREMIUM".equalsIgnoreCase(accountType)) {
            return strategies.get("percentageFeeStrategy");
        }
        return strategies.get("fixedFeeStrategy");
    }

}