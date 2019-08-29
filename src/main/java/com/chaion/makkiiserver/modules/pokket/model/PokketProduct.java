package com.chaion.makkiiserver.modules.pokket.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PokketProduct {
    private Long productId;
    private String token;
    private String tokenFullName;
    private BigDecimal yearlyInterestRate;
    private BigDecimal weeklyInterestRate;
    private BigDecimal remainingQuota;
    private BigDecimal minInvestAmount;
    private BigDecimal token2Collateral;
}
