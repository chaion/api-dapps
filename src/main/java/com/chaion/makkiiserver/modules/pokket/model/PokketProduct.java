package com.chaion.makkiiserver.modules.pokket.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PokketProduct {
    @ApiModelProperty(value="产品Id")
    private Long productId;
    @ApiModelProperty(value = "投资币种代号")
    private String token;
    @ApiModelProperty(value="投资币种全称")
    private String tokenFullName;
    @ApiModelProperty(value = "年利息")
    private BigDecimal yearlyInterestRate;
    @ApiModelProperty(value = "周利息")
    private BigDecimal weeklyInterestRate;
    @ApiModelProperty(value = "当日剩余额额度")
    private BigDecimal remainingQuota;
    @ApiModelProperty(value = "最低投资额度")
    private BigDecimal minInvestAmount;
    @ApiModelProperty(value = "投资币种兑TUSD")
    private BigDecimal token2Collateral;
    @ApiModelProperty(value = "允许的小数点位数")
    private Integer allowedDecimals;
}
