package com.chaion.makkiiserver.blockchain;

import lombok.Data;

@Data
public class TransactionConfirmEvent {
    private TransactionStatus status;

    public TransactionConfirmEvent(TransactionStatus status) {
        this.status = status;
    }
}
