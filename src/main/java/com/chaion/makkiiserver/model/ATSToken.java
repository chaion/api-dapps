package com.chaion.makkiiserver.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Field;

@ApiModel(description="All details about aion token standard")
public class ATSToken {
    @ApiModelProperty(example = "Musiq")
    private String name;

    @ApiModelProperty(example = "MSQ")
    private String symbol;

    @ApiModelProperty(example = "0xa00fd8b28209d167f14742edf3cabb154aa3e7ec3402669e5bec7e6c49f8b313")
    @Field("contractAddress")
    private String contractAddr;

    @ApiModelProperty(example = "18")
    @Field("decimals")
    private String tokenDecimal;

    @ApiModelProperty(example = "100000000000000000000000")
    private String totalSupply;

    @ApiModelProperty(example = "100000000000000000000000")
    private String liquidSupply;

    @ApiModelProperty(example = "0xa060a85c0d01e03ca69a0dc1bcba6fe29696317df2b6d5dd798ee5001d586d67")
    private String creatorAddress;

    @ApiModelProperty(example = "0xf42947d0c5f1533bd3f0c99d5b616f2d5283b1f61bdbc6ff4ecd42d642e757df")
    private String transactionHash;

    @ApiModelProperty(example = "1")
    private String granularity;

    @ApiModelProperty(example = "1543832698")
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
