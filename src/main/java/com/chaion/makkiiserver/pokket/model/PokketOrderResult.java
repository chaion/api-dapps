package com.chaion.makkiiserver.pokket.model;

public enum PokketOrderResult {
    /**
     * <10% and no auto roll
     */
    LESS_THAN_NO_ROLL,
    /**
     * <10% and auto roll
     */
    LESS_THAN_ROLL,
    /**
     * >=10%
     */
    GREATER_THAN
}
