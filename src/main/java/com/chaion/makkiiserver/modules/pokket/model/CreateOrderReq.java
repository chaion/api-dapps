package com.chaion.makkiiserver.modules.pokket.model;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreateOrderReq {
    @ApiModelProperty(value="投资者地址（以太坊或比特币地址）")
    @NotEmpty
    private String investorAddress;
    @ApiModelProperty(value="投资币种代号", notes = "应与产品列表中的token字段一致")
    @NotEmpty
    private String token;
    @ApiModelProperty(value="投资币种全称", notes = "应与产品列表中的tokenFullName字段一致")
    private String tokenFullName;
    @ApiModelProperty(value="投资金额")
    @NotNull
    private BigDecimal amount;
    @ApiModelProperty(value="备用金返回地址",
            notes = "当币价涨幅超过10%时，返还TUSD到该地址。" +
                    "投资币种为比特币时，为必填字段。" +
                    "投资币为以太坊或ERC20时，可为空，默认将返还TUSD指投资地址。")
    private String collateralAddress;
    @ApiModelProperty(value="周利率")
    @NotNull
    private BigDecimal weeklyInterestRate;
    @ApiModelProperty(value="年利率",notes = "年利率=周利率×52")
    @NotNull
    private BigDecimal yearlyInterestRate;
    @ApiModelProperty(value="投资币/TUSD", notes = "该值为前一天的汇率")
    @NotNull
    private BigDecimal token2Collateral;
    @ApiModelProperty(value="产品Id")
    @NotNull
    private Long productId;
    @ApiModelProperty(value="raw交易数据", notes = "投资者转账至Pokket储蓄账户的交易哈希")
    @NotEmpty
    private String rawTransaction;
    @ApiModelProperty(value="允许小数点位数")
    private Integer allowedDecimals;
}
