package com.chaion.makkiiserver.pokket.model;

public class SetAutoRollReq {

    private String orderId;
    private boolean autoRoll;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public boolean isAutoRoll() {
        return autoRoll;
    }

    public void setAutoRoll(boolean autoRoll) {
        this.autoRoll = autoRoll;
    }
}
