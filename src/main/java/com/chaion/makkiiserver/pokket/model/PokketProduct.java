package com.chaion.makkiiserver.pokket.model;

import java.math.BigDecimal;

public class PokketProduct {
    private Long productId;
    private String token;
    private String tokenFullName;
    private BigDecimal yearlyInterestRate;
    private BigDecimal weeklyInterestRate;
    private BigDecimal remainingQuota;
    private BigDecimal minInvestAmount;
    private BigDecimal token2Collateral;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenFullName() {
        return tokenFullName;
    }

    public void setTokenFullName(String tokenFullName) {
        this.tokenFullName = tokenFullName;
    }

    public BigDecimal getYearlyInterestRate() {
        return yearlyInterestRate;
    }

    public void setYearlyInterestRate(BigDecimal yearlyInterestRate) {
        this.yearlyInterestRate = yearlyInterestRate;
    }

    public BigDecimal getWeeklyInterestRate() {
        return weeklyInterestRate;
    }

    public void setWeeklyInterestRate(BigDecimal weeklyInterestRate) {
        this.weeklyInterestRate = weeklyInterestRate;
    }

    public BigDecimal getRemainingQuota() {
        return remainingQuota;
    }

    public void setRemainingQuota(BigDecimal remainingQuota) {
        this.remainingQuota = remainingQuota;
    }

    public BigDecimal getMinInvestAmount() {
        return minInvestAmount;
    }

    public void setMinInvestAmount(BigDecimal minInvestAmount) {
        this.minInvestAmount = minInvestAmount;
    }

    public BigDecimal getToken2Collateral() {
        return token2Collateral;
    }

    public void setToken2Collateral(BigDecimal token2Collateral) {
        this.token2Collateral = token2Collateral;
    }
}
