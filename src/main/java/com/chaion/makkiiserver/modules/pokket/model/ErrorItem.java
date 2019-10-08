package com.chaion.makkiiserver.modules.pokket.model;

import lombok.Data;

@Data
public class ErrorItem {
    private Long time;
    private String errorMessage;
    private boolean isResolved;
    private PokketOrderStatus currentStatus;
}
