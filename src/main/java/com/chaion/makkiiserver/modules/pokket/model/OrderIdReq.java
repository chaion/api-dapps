package com.chaion.makkiiserver.modules.pokket.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OrderIdReq {
    @ApiModelProperty(value = "Pokket订单号")
    private String externalOrderId;
    @ApiModelProperty(value = "订单号")
    private Long orderId;
    @ApiModelProperty(value = "结果")
    private Boolean success;
    @ApiModelProperty(value = "错误原因")
    private String errorMessage;
}
