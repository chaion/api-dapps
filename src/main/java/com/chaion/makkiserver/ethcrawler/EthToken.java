package com.chaion.makkiserver.ethcrawler;

import org.springframework.data.mongodb.core.mapping.Field;

public class EthToken {
    private String imagePath;
    private String name;
    private String symbol;
    private String desc;
    @Field("contractAddress")
    private String contractAddr;
    @Field("decimals")
    private String tokenDecimal;
    private String officialSite;
    private String totalSupply;
    private String contractType;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public String getOfficialSite() {
        return officialSite;
    }

    public void setOfficialSite(String officialSite) {
        this.officialSite = officialSite;
    }

    public String getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(String totalSupply) {
        this.totalSupply = totalSupply;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }
}
