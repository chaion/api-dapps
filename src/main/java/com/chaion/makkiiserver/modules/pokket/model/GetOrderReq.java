package com.chaion.makkiiserver.modules.pokket.model;

import lombok.Data;

import java.util.List;

@Data
public class GetOrderReq {
    private List<String> addresses;
    private int page;
    private int size;
}
