package com.chaion.makkiiserver.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

@ApiModel(description="cryptocurrency/fiat currency exchange rate model")
public class MarketPrice {

    @ApiModelProperty(value="crypto currency symbol", example="AION")
    private String crypto;

    @ApiModelProperty(value="fiat currency symbol", example="CNY")
    private String fiat;

    @ApiModelProperty(value="unit price of fiat currency one crypto currency can exchange with", example = "0.82")
    private BigDecimal price;

    public String getCrypto() {
        return crypto;
    }

    public void setCrypto(String crypto) {
        this.crypto = crypto;
    }

    public String getFiat() {
        return fiat;
    }

    public void setFiat(String fiat) {
        this.fiat = fiat;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "MarketPrice{" +
                "crypto='" + crypto + '\'' +
                ", fiat='" + fiat + '\'' +
                ", price=" + price +
                '}';
    }
}
