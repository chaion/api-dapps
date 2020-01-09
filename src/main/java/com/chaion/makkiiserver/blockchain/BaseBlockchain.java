package com.chaion.makkiiserver.blockchain;

import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseBlockchain implements BlockchainService {

    private static final Logger logger = LoggerFactory.getLogger(BaseBlockchain.class);
    @Autowired
    protected TransactionStatusRepository txStatusRepo;
    @Autowired
    protected EventBus eventBus;

    /**
     * add transaction to pending queue
     *
     * @param transactionId
     */
    public void addPendingTransaction(String transactionId, Map<String, Object> customData) {
        TransactionStatus ts = new TransactionStatus(getName(), transactionId, customData);
        txStatusRepo.save(ts);
    }

    /**
     * check pending transaction status periodly.
     */
    public void checkPendingTxsStatus() {
        logger.info("check pending eth tx status...");
        List<String> statusIn = new ArrayList<>();
        statusIn.add(TransactionStatus.WAIT_RECEIPT);
        statusIn.add(TransactionStatus.WAIT_BLOCK_NUMBER);
        List<TransactionStatus> pendingTransactions = txStatusRepo.findByChainNameAndStatusIn(getName(), statusIn);
        for (TransactionStatus ts : pendingTransactions) {
            if (ts.isExpire()) {
                ts.setStatus(TransactionStatus.TIMEOUT);
                txStatusRepo.save(ts);
                logger.info("send transaction confirm event: " + ts);
                eventBus.post(new TransactionConfirmEvent(ts));
            } else {
                checkPendingTxStatus(ts);
            }
        }
    }

    protected abstract void checkPendingTxStatus(TransactionStatus ts);
}
