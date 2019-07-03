package com.chaion.makkiiserver.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Field;

@ApiModel(description="Eth Token")
public class EthToken {

    @ApiModelProperty(example = "/token/images/bnb_28_2.png")
    private String imagePath;

    @ApiModelProperty(example = "BNB")
    private String name;

    @ApiModelProperty(example = "BNB")
    private String symbol;

    @ApiModelProperty(example = "Binance aims to build a world-class crypto exchange, powering the future of crypto finance.")
    private String desc;

    @ApiModelProperty(example = "0xB8c77482e45F1F44dE1745F52C74426C631bDD52")
    @Field("contractAddress")
    private String contractAddr;

    @ApiModelProperty(example = "18")
    @Field("decimals")
    private String tokenDecimal;

    @ApiModelProperty(example = "https://www.binance.com/", allowEmptyValue = true)
    private String officialSite;

    @ApiModelProperty(example = "16,579,517.055253348798759097")
    private String totalSupply;

    @ApiModelProperty(value="token contract type, such as ERC-20", allowableValues = "ERC-20")
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

    @Override
    public String toString() {
        return "EthToken{" +
                "\nimagePath='" + imagePath + '\'' +
                ", \nname='" + name + '\'' +
                ", \nsymbol='" + symbol + '\'' +
                ", \ndesc='" + desc + '\'' +
                ", \ncontractAddr='" + contractAddr + '\'' +
                ", \ntokenDecimal='" + tokenDecimal + '\'' +
                ", \nofficialSite='" + officialSite + '\'' +
                ", \ntotalSupply='" + totalSupply + '\'' +
                ", \ncontractType='" + contractType + '\'' +
                "\n}";
    }
}
