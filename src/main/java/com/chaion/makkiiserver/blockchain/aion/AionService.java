package com.chaion.makkiiserver.blockchain.aion;

import com.chaion.makkiiserver.blockchain.BaseBlockchain;
import com.chaion.makkiiserver.blockchain.BlockchainException;
import com.chaion.makkiiserver.blockchain.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.aion.AionConstants;
import org.web3j.aion.VirtualMachine;
import org.web3j.aion.crypto.Ed25519KeyPair;
import org.web3j.aion.protocol.Aion;
import org.web3j.aion.tx.AionTransactionManager;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;

@Service
public class AionService extends BaseBlockchain {

    private static final Long DEFAULT_GAS_PRICE = 2l * (long) Math.pow(10,9);

    private static final Logger logger = LoggerFactory.getLogger(AionService.class);

    private Aion aion;

    public AionService(@Value("${blockchain.aion.apiserver}") String rpcServer) {
        aion = Aion.build(new HttpService(rpcServer));
    }

    /**
     * Send transaction
     *
     * @param privateKey
     * @param toAddress
     * @param amount
     * @return
     * @throws BlockchainException
     */
    public String sendTransaction(String privateKey, String toAddress, BigInteger amount) throws BlockchainException {
        logger.info("[aion][sendTransaction] transfer {} aion to {}", amount, toAddress);
        AionTransactionManager manager = new AionTransactionManager(aion,
                new Ed25519KeyPair(privateKey), VirtualMachine.FVM);
        BigInteger gasPrice = BigInteger.valueOf(DEFAULT_GAS_PRICE);
        BigInteger gasLimit = BigInteger.valueOf(AionConstants.NRG_TRANSACTION_MIN);
        try {
            EthSendTransaction tx = manager.sendTransaction(gasPrice, gasLimit, toAddress, "0x", amount, Boolean.FALSE);
            if (tx.hasError()) {
                Response.Error error = tx.getError();
                logger.error("[aion][sendTransaction] failed: ", error);
                throw new BlockchainException(String.format("error code: {}, message: {}, data: {}",
                        error.getCode(), error.getMessage(), error.getData()));
            }
            String txHash = tx.getTransactionHash();
            logger.info("[aion][sendTransaction] txId=" + txHash);
            return txHash;
        } catch (Exception e) {
            logger.error("[aion][sendTransaction] exception: ", e);
            throw new BlockchainException(e.getMessage());
        }
    }

    @Override
    public void checkPendingTxStatus() {
        logger.info("check pending aion tx status...");
        for (Map.Entry<String, TransactionStatus> entry : pendingTransactions.entrySet()) {
            TransactionStatus st = entry.getValue();
            String txHash = st.getTxId();
            logger.info("processing tx: " + txHash + " " + st.getStatus());
            if (st.getStatus().equalsIgnoreCase(TransactionStatus.WAIT_RECEIPT)) {
                checkTxStatusOfReceipt(st, txHash);
            }
        }
    }

    public TransactionReceipt getTransactionReceipt(String txHash) throws BlockchainException {
        EthGetTransactionReceipt resp = null;
        try {
            resp = aion.ethGetTransactionReceipt(txHash).sendAsync().get();
        } catch (Exception e) {
            throw new BlockchainException(e.getMessage());
        }
        if (resp.hasError()) {
            throw new BlockchainException(resp.getError().toString());
        }
        Optional<TransactionReceipt> receiptOpt = resp.getTransactionReceipt();
        if (!receiptOpt.isPresent()) {
            return null;
        }
        return receiptOpt.get();
    }

    private void checkTxStatusOfReceipt(TransactionStatus st, String txHash) {
        TransactionReceipt receipt = null;
        try {
            receipt = getTransactionReceipt(txHash);
        } catch (Exception e) {
            // ignore exception
            return;
        }
        if (receipt == null) return;
        logger.info("tx[" + txHash + "] receipt is confirmed " + receipt.isStatusOK());
        st.setStatus(TransactionStatus.CONFIRMED);
        pendingTransactions.remove(txHash);
        st.getListener().transactionConfirmed(txHash, receipt.isStatusOK());
    }

    @Override
    public String sendRawTransaction(String rawTransaction) throws BlockchainException {
        return null;
    }
}
