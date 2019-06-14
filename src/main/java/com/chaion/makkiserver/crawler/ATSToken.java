package com.chaion.makkiserver.crawler;

import org.springframework.data.mongodb.core.mapping.Field;

public class ATSToken {
    private String name;
    private String symbol;
    @Field("contractAddress")
    private String contractAddr;
    @Field("decimals")
    private String tokenDecimal;
    private String totalSupply;
    private String liquidSupply;
    private String creatorAddress;
    private String transactionHash;
    private String granularity;
    private String creationTimestamp;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getContractAddr() {
        return contractAddr;
    }

    public void setContractAddr(String contractAddr) {
        this.contractAddr = contractAddr;
    }

    public String getTokenDecimal() {
        return tokenDecimal;
    }

    public void setTokenDecimal(String tokenDecimal) {
        this.tokenDecimal = tokenDecimal;
    }

    public String getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(String totalSupply) {
        this.totalSupply = totalSupply;
    }

    public String getLiquidSupply() {
        return liquidSupply;
    }

    public void setLiquidSupply(String liquidSupply) {
        this.liquidSupply = liquidSupply;
    }

    public String getCreatorAddress() {
        return creatorAddress;
    }

    public void setCreatorAddress(String creatorAddress) {
        this.creatorAddress = creatorAddress;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public String getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(String creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    @Override
    public String toString() {
        return "ATSToken{" +
                "\nname='" + name + '\'' +
                "\n, symbol='" + symbol + '\'' +
                "\n, contractAddr='" + contractAddr + '\'' +
                "\n, tokenDecimal='" + tokenDecimal + '\'' +
                "\n, totalSupply='" + totalSupply + '\'' +
                "\n, liquidSupply='" + liquidSupply + '\'' +
                "\n, creatorAddress='" + creatorAddress + '\'' +
                "\n, transactionHash='" + transactionHash + '\'' +
                "\n, granularity='" + granularity + '\'' +
                "\n, creationTimestamp='" + creationTimestamp + '\'' +
                "\n}";
    }
}
