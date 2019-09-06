package com.chaion.makkiiserver.blockchain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseBlockchain implements BlockchainService {

    /**
     * pending transaction list. scheduled task will check transactions' status.
     */
    protected Map<String, TransactionStatus> pendingTransactions = new ConcurrentHashMap<>();

    /**
     * add transaction to pending queue
     *
     * @param transactionId
     * @param listener
     */
    public void addPendingTransaction(String transactionId, TransactionStatusListener listener) {
        pendingTransactions.put(transactionId, new TransactionStatus(transactionId, listener));
    }

    /**
     * check pending transaction status periodly.
     */
    public abstract void checkPendingTxStatus();
}
