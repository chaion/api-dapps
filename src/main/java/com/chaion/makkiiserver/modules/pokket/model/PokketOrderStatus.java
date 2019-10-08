package com.chaion.makkiiserver.modules.pokket.model;

public enum PokketOrderStatus {
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
