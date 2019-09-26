package com.chaion.makkiiserver.modules.pokket.model;

import lombok.Data;

import java.util.List;

@Data
public class QueryOrderReq {
    private String orderId;
    private Long pokketOrderId;
    private Long fromTime;
    private Long toTime;
    private List<String> investorAddresses;
    private PokketOrderStatus status;
    private String token;

    private int page;
    private int size;
}
