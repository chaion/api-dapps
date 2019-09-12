package com.chaion.makkiiserver.modules.pokket.model;

import lombok.Data;

@Data
public class QueryOrderReq {
    private Long startTime;
    private Long endTime;
    private int page;
    private int size;
}
