package com.chaion.makkiiserver.modules.pokket.model;

import lombok.Data;

import java.math.BigInteger;

@Data
public class TransactionStatus {

    /**
     * if tx is in this status, next is to get transaction receipt
     */
    public static final String WAIT_RECEIPT = "wait_for_receipt";
    /**
     * if tx is in this status, next is to query block number for confirmations.
     */
    public static final String WAIT_BLOCK_NUMBER = "wait_for_block_number";
    /**
     * if tx is in this status, transaction is confirmed as ok or failure.
     */
    public static final String CONFIRMED = "confirmed";

    private String txHash;
    private BigInteger blockNumber;
    private String status;
    private TransactionStatusListener listener;

    public TransactionStatus(String txHash, TransactionStatusListener listener) {
        this.txHash = txHash;
        this.listener = listener;
        this.status = WAIT_RECEIPT;
    }
}
