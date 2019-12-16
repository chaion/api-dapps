package com.chaion.makkiiserver.modules.pokket.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class CollateralSettlementReq {

    @ApiModelProperty(value="当天新订单号列表")
    private List<String> newOrderIds;

    @ApiModelProperty(value="当天结束的订单号列表(需将TUSD给用户)")
    private List<String> closedOrderIdsYieldTUSD;

    @ApiModelProperty(value="当天结束的订单号列表(需退还TUSD给Pokket)")
    private List<String> closedOrderIdsReturnTUSD;

    @ApiModelProperty(value="当天清算后，备用金取出或存入Audit钱包的交易哈希")
    private String transactionHash;

    @ApiModelProperty(value="当天3点时token兑TUSD汇率")
    private Map<String, BigDecimal> token2CollateralMap;

}
