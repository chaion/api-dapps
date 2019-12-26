package com.chaion.makkiiserver.modules.pokket.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Data
public class PokketOrder {

    @ApiModelProperty(value = "订单号", notes = "Makkii服务端生成订单号")
    @Id
    private String orderId;
    @ApiModelProperty(value = "Pokket端订单号")
    private Long pokketOrderId;
    @ApiModelProperty(value = "投资者地址")
    private String investorAddress;
    @ApiModelProperty(value = "备用金地址")
    private String collateralAddress;
    @ApiModelProperty(value = "投资代币代号")
    private String token;
    @ApiModelProperty(value = "投资代币全称")
    private String tokenFullName;
    @ApiModelProperty(value = "投资金额")
    private BigDecimal amount;
    @ApiModelProperty(value = "投资代币兑TUSD")
    private BigDecimal token2Collateral;
    @ApiModelProperty(value = "周利息")
    private BigDecimal weeklyInterestRate;
    @ApiModelProperty(value = "年利息")
    private BigDecimal yearlyInterestRate;
    @ApiModelProperty(value = "投资交易哈希")
    private String investTransactionHash;
    @ApiModelProperty(value = "Pokket存入备用金的交易哈希")
    private String depositTUSDTransactionHash;
    @ApiModelProperty(value = "返还用户投资币的交易哈希")
    private String yieldTokenTransactionHash;
    @ApiModelProperty(value = "返还Pokket TUSD的交易哈希")
    private String returnTUSDTransactionHash;
    @ApiModelProperty(value = "返还用户TUSD的交易哈希")
    private String yieldTUSDTransactionHash;
    @ApiModelProperty(value = "订单生成时间", notes = "毫秒")
    private Long createTime;
    @ApiModelProperty(value = "订单状态")
    private PokketOrderStatus status;
    @ApiModelProperty(value = "订单完成结果")
    private PokketOrderResult result;
    @ApiModelProperty(value = "订单开始时间")
    private Long startTime;
    @ApiModelProperty(value = "产品Id")
    private Long productId;
    @ApiModelProperty(value = "错误历史")
    private List<ErrorItem> errors;
    @ApiModelProperty(value = "实际返还用户金额,可为投资币种或TUSD")
    private BigInteger actualAmount;

    public void addErrorItem(String errorMessage) {
        ErrorItem errorItem = new ErrorItem();
        errorItem.setErrorMessage(errorMessage);
        errorItem.setResolved(false);
        errorItem.setTime(System.currentTimeMillis());
        errorItem.setCurrentStatus(status);
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(errorItem);
    }


}
