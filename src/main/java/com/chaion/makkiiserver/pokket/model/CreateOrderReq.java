package com.chaion.makkiiserver.pokket.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateOrderReq {
    @NotEmpty
    private String investorAddress;
    @NotEmpty
    private String token;
    private String tokenFullName;
    @NotNull
    private BigDecimal amount;
    private String collateralAddress;
    @NotNull
    private BigDecimal weeklyInterestRate;
    @NotNull
    private BigDecimal yearlyInterestRate;
    @NotNull
    private BigDecimal token2Collateral;
    private boolean autoRoll;
    @NotNull
    private Long productId;
    @NotEmpty
    private String rawTransaction;

    public String getInvestorAddress() {
        return investorAddress;
    }

    public void setInvestorAddress(String investorAddress) {
        this.investorAddress = investorAddress;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCollateralAddress() {
        return collateralAddress;
    }

    public void setCollateralAddress(String collateralAddress) {
        this.collateralAddress = collateralAddress;
    }

    public BigDecimal getWeeklyInterestRate() {
        return weeklyInterestRate;
    }

    public void setWeeklyInterestRate(BigDecimal weeklyInterestRate) {
        this.weeklyInterestRate = weeklyInterestRate;
    }

    public BigDecimal getYearlyInterestRate() {
        return yearlyInterestRate;
    }

    public void setYearlyInterestRate(BigDecimal yearlyInterestRate) {
        this.yearlyInterestRate = yearlyInterestRate;
    }

    public BigDecimal getToken2Collateral() {
        return token2Collateral;
    }

    public void setToken2Collateral(BigDecimal token2Collateral) {
        this.token2Collateral = token2Collateral;
    }

    public boolean isAutoRoll() {
        return autoRoll;
    }

    public void setAutoRoll(boolean autoRoll) {
        this.autoRoll = autoRoll;
    }

    public String getRawTransaction() {
        return rawTransaction;
    }

    public void setRawTransaction(String rawTransaction) {
        this.rawTransaction = rawTransaction;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "CreateOrderReq{" +
                "investorAddress='" + investorAddress + '\'' +
                ", token='" + token + '\'' +
                ", tokenFullName='" + tokenFullName + '\'' +
                ", amount=" + amount +
                ", collateralAddress='" + collateralAddress + '\'' +
                ", weeklyInterestRate=" + weeklyInterestRate +
                ", yearlyInterestRate=" + yearlyInterestRate +
                ", token2Collateral=" + token2Collateral +
                ", autoRoll=" + autoRoll +
                ", productId=" + productId +
                ", rawTransaction='" + rawTransaction + '\'' +
                '}';
    }
}
