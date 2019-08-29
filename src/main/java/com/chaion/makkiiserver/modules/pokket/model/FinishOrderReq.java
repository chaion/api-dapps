package com.chaion.makkiiserver.modules.pokket.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FinishOrderReq {
    @ApiModelProperty(value="订单号")
    private String orderId;
    @ApiModelProperty(value="订单结果", notes="LESS_THAN_NO_ROLL: <10%, GREATER_THAN: >= 10%")
    private PokketOrderResult result;
    @ApiModelProperty(value="返利投资币的交易哈希", notes="RESULT为LESS_THAN_NO_ROLL时传")
    private String txHashYieldToken;
    @ApiModelProperty(value="退回TUSD的交易哈希", notes="RESULT为LESS_THAN_NO_ROLL时传")
    private String txHashReturnTUSD;
    @ApiModelProperty(value="返利TUSD的交易哈希", notes="RESULT为GREATER_THAN时传")
    private String txHashYieldTUSD;
}
