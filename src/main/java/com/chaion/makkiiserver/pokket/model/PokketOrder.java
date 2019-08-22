package com.chaion.makkiiserver.pokket.model;

import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

public class PokketOrder {

    @Id
    private String orderId;
    private String investorAddress;
    private String collateralAddress;
    private String token;
    private String tokenFullName;
    private BigDecimal amount;
    private BigDecimal token2Collateral;
    private BigDecimal weeklyInterestRate;
    private BigDecimal yearlyInterestRate;
    private String investTransactionHash;
    private String depositTUSDTransactionHash;
    private String yieldTokenTransactionHash;
    private String returnTUSDTransactionHash;
    private String yieldTUSDTransactionHash;
    private Long createTime;
    private PokketOrderStatus status;
    private PokketOrderResult result;
    private Long startTime;
    private Long productId;
    private String previousOrderId;
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

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

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getPreviousOrderId() {
        return previousOrderId;
    }

    public void setPreviousOrderId(String previousOrderId) {
        this.previousOrderId = previousOrderId;
    }

    public String getInvestTransactionHash() {
        return investTransactionHash;
    }

    public void setInvestTransactionHash(String investTransactionHash) {
        this.investTransactionHash = investTransactionHash;
    }

    public BigDecimal getToken2Collateral() {
        return token2Collateral;
    }

    public void setToken2Collateral(BigDecimal token2Collateral) {
        this.token2Collateral = token2Collateral;
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

    public String getDepositTUSDTransactionHash() {
        return depositTUSDTransactionHash;
    }

    public void setDepositTUSDTransactionHash(String depositTUSDTransactionHash) {
        this.depositTUSDTransactionHash = depositTUSDTransactionHash;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public PokketOrderStatus getStatus() {
        return status;
    }

    public void setStatus(PokketOrderStatus status) {
        this.status = status;
    }

    public String getYieldTokenTransactionHash() {
        return yieldTokenTransactionHash;
    }

    public void setYieldTokenTransactionHash(String yieldTokenTransactionHash) {
        this.yieldTokenTransactionHash = yieldTokenTransactionHash;
    }

    public String getReturnTUSDTransactionHash() {
        return returnTUSDTransactionHash;
    }

    public void setReturnTUSDTransactionHash(String returnTUSDTransactionHash) {
        this.returnTUSDTransactionHash = returnTUSDTransactionHash;
    }

    public String getYieldTUSDTransactionHash() {
        return yieldTUSDTransactionHash;
    }

    public void setYieldTUSDTransactionHash(String yieldTUSDTransactionHash) {
        this.yieldTUSDTransactionHash = yieldTUSDTransactionHash;
    }

    public PokketOrderResult getResult() {
        return result;
    }

    public void setResult(PokketOrderResult result) {
        this.result = result;
    }

    public String getCollateralAddress() {
        return collateralAddress;
    }

    public void setCollateralAddress(String collateralAddress) {
        this.collateralAddress = collateralAddress;
    }
}
