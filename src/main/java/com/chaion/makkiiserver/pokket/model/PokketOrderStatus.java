package com.chaion.makkiiserver.pokket.model;

public enum PokketOrderStatus {
    ERROR,
    /**
     * after investor sends transaction, and wait for transaction to confirm.
     */
    WAIT_INVEST_TX_CONFIRM,
    /**
     * wait pokket to deposit collateral
     */
    WAIT_COLLATERAL_DEPOSIT,
    /**
     * in progress
     */
    IN_PROGRESS,
    /**
     * completed
     */
    COMPLETE
}
