package com.chaion.makkiiserver.modules.analytics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="Transfer statistic model pertaining to a certain coin/token")
public class CoinTransferInfo {

    @ApiModelProperty(value = "Native Coin", example = "AION")
    private String coin;

    @ApiModelProperty(value = "Token issued under native chain",
            example = "MAK",
            allowEmptyValue = true)
    private String token;

    @ApiModelProperty(value = "number of coin or tokens that transferred", example = "1")
    private long totalCount;

    @ApiModelProperty(value = "total amount of coin or tokens that transferred", example = "20")
    private double totalAmount;

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
