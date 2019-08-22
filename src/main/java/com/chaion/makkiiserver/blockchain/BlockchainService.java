package com.chaion.makkiiserver.blockchain;

import com.chaion.makkiiserver.model.EthToken;
import com.chaion.makkiiserver.repository.EthTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeDecoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlockchainService {

    private static final Logger logger = LoggerFactory.getLogger(BlockchainService.class);

    @Autowired
    EthTokenRepository ethTokenRepo;

    private Web3j ethWeb3j;
    private Map<String, TransactionReceiptStatus> pendingTransactions = new ConcurrentHashMap<>();
    class TransactionReceiptStatus {
        static final String WAIT_RECEIPT = "wait_for_receipt";
        static final String WAIT_BLOCK_NUMBER = "wait_for_block_number";
        static final String CONFIRMED = "confirmed";
        TransactionReceiptStatus(String txHash, TransactionListener listener) {
            this.txHash = txHash;
            this.listener = listener;
            this.status = WAIT_RECEIPT;
        }
        private String txHash;
        private BigInteger blockNumber;
        private String status;
        private TransactionListener listener;

        public BigInteger getBlockNumber() {
            return blockNumber;
        }

        public String getStatus() {
            return status;
        }

        public void setBlockNumber(BigInteger blockNumber) {
            this.blockNumber = blockNumber;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public BlockchainService(@Value("${blockchain.eth.rpcserver}") String rpcServer) {
        logger.info("initialize blockchain service: " + rpcServer);
        ethWeb3j = Web3j.build(new HttpService(rpcServer));
    }

    public String sendRawTransaction(String rawTx) throws BlockchainException {
        EthSendTransaction sendTxResp = null;
        try {
            sendTxResp = ethWeb3j.ethSendRawTransaction(rawTx).sendAsync().get();
            if (sendTxResp.hasError()) {
                Response.Error error = sendTxResp.getError();
                logger.error("sendrawtransaction failed: ", error);
                throw new BlockchainException(error.getCode() + " " + error.getMessage());
            }
            return sendTxResp.getTransactionHash();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new BlockchainException(e.getMessage());
        }
    }

    public interface TransactionListener {
        void transactionConfirmed(String transactionHash, boolean status);
    }

    public void addPendingTransaction(String transactionHash, TransactionListener listener) {
        TransactionReceiptStatus st = new TransactionReceiptStatus(transactionHash, listener);
        pendingTransactions.put(transactionHash, st);
    }

    public void checkPendingTxStatus() {
        for (Map.Entry<String, TransactionReceiptStatus> entry : pendingTransactions.entrySet()) {
            TransactionReceiptStatus st = entry.getValue();
            if (st.status.equalsIgnoreCase(TransactionReceiptStatus.WAIT_RECEIPT)) {
                EthGetTransactionReceipt receiptResp = null;
                try {
                    receiptResp = ethWeb3j.ethGetTransactionReceipt(st.txHash).sendAsync().get();
                } catch (Exception e) {
                    logger.error("get transaction receipt exception: ", e);
                    continue;
                }
                if (receiptResp.hasError()) {
                    logger.debug("get transaction receipt - " + st.txHash + ", error: " + receiptResp.getError());
                    continue;
                }
                Optional<TransactionReceipt> receiptOpt = receiptResp.getTransactionReceipt();
                if (!receiptOpt.isPresent()) {
                    logger.debug("get transaction receipt - " + st.txHash + " is not present");
                    continue;
                }
                TransactionReceipt receipt = receiptOpt.get();
                if (receipt.isStatusOK()) {
                    st.setBlockNumber(receipt.getBlockNumber());
                    st.setStatus(TransactionReceiptStatus.WAIT_BLOCK_NUMBER);
                } else {
                    st.listener.transactionConfirmed(st.txHash, false);
                    pendingTransactions.remove(st.txHash);
                }
            } else if (st.status.equalsIgnoreCase(TransactionReceiptStatus.WAIT_BLOCK_NUMBER)) {
                EthBlockNumber blockNumberResp = null;
                try {
                    blockNumberResp = ethWeb3j.ethBlockNumber().sendAsync().get();
                } catch (Exception e) {
                    logger.debug("get block number exception: ", e);
                    continue;
                }
                if (blockNumberResp.hasError()) {
                    logger.debug("block number - " + st.txHash + ", error: " + blockNumberResp.getError());
                    continue;
                }
                BigInteger blockNumber = blockNumberResp.getBlockNumber();
                if (blockNumber.subtract(st.getBlockNumber()).longValue() > 6) {
                    st.setStatus(TransactionReceiptStatus.CONFIRMED);
                    st.listener.transactionConfirmed(st.txHash, true);
                    pendingTransactions.remove(st.txHash);
                }
            }
        }
    }

    public boolean validateERC20Transaction(String transactionHash,
                                            String expectedFrom, String expectedTo,
                                            String token,
                                            BigDecimal expectedAmount) {
        EthGetTransactionReceipt receiptResp = null;
        try {
            receiptResp = ethWeb3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();
        } catch (Exception e) {
            logger.error("validate failed: get transaction receipt", e);
            return false;
        }
        if (receiptResp.hasError()) {
            logger.error("validate failed: getTransactionReceipt has error", receiptResp.getError());
            return false;
        }
        Optional<TransactionReceipt> receiptOpt = receiptResp.getTransactionReceipt();
        if (!receiptOpt.isPresent()) {
            logger.error("validate failed: get transaction receipt is not present");
            return false;
        }
        TransactionReceipt receipt = receiptOpt.get();
        if (!receipt.getFrom().equalsIgnoreCase(expectedFrom)) {
            logger.error("validate failed: expected from " + expectedFrom + ", actual from " + receipt.getFrom());
            return false;
        }
        List<EthToken> ethTokens = ethTokenRepo.findBySymbol(token);
        boolean isToAddressCorrect = false;
        for (EthToken ethToken : ethTokens) {
            if (ethToken.getContractAddr().equalsIgnoreCase(receipt.getTo())) {
                isToAddressCorrect = true;
                break;
            }
        }
        if (!isToAddressCorrect) {
            logger.error("validate failed: " + receipt.getTo() + " is not " + token + "'s contract address.");
            return false;
        }
        if (!receipt.isStatusOK()) {
            logger.error("validate failed: receipt status is not ok");
            return false;
        }

        EthTransaction transactionResp = null;
        try {
            transactionResp = ethWeb3j.ethGetTransactionByHash(transactionHash).sendAsync().get();
        } catch (Exception e) {
            logger.error("validate failed: get transaction by hash failed.", e);
            return false;
        }
        if (transactionResp.hasError()) {
            logger.error("validate failed: get transaction by hash has error", transactionResp.getError());
            return false;
        }
        Optional<Transaction> transactionOpt = transactionResp.getTransaction();
        if (!transactionOpt.isPresent()) {
            logger.error("validate failed: get transaction is not present.");
            return false;
        }
        Transaction transaction = transactionOpt.get();
        String input = transaction.getInput();
        try {
            // decode input data
            String method = input.substring(0, 10);
            logger.info("method: " + method);
            String to = input.substring(10, 74);
            String value = input.substring(74);
            Method refMethod = null;
            refMethod = TypeDecoder.class.getDeclaredMethod("decode", String.class, int.class, Class.class);
            refMethod.setAccessible(true);
            Address address = (Address) refMethod.invoke(null, to, 0, Address.class);
            Uint256 amount = (Uint256) refMethod.invoke(null, value, 0, Uint256.class);
            // validate amount and to in input
            if (!address.getValue().equalsIgnoreCase(expectedTo)) {
                logger.error("validate failed: expected to is " + expectedTo + ", actual is " + address.getValue());
                return false;
            }
            if (amount.getValue().compareTo(expectedAmount.scaleByPowerOfTen(18).toBigInteger()) != 0) {
                logger.error("validate failed: expected amount is " +expectedAmount.scaleByPowerOfTen(18) + ", actual amount is" + amount.getValue());
                return false;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("validated failed: decode input data failed");
            return false;
        }
        return true;
    }

    public boolean validateEthTx(String transactionHash, String expectedFrom, String expectedTo, BigDecimal expectedAmount) {
        EthGetTransactionReceipt receiptResp = null;
        try {
            receiptResp = ethWeb3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();
        } catch (Exception e) {
            logger.error("validate failed: getTransactionReceipt exception: ", e);
            return false;
        }
        logger.debug("receiptResp: " + receiptResp);
        if (receiptResp.hasError()) {
            logger.error("validate failed: getTransactionReceipt has error");
            return false;
        }
        Optional<TransactionReceipt> receiptOpt = receiptResp.getTransactionReceipt();
        if (!receiptOpt.isPresent()) {
            logger.error("validate failed: get transaction receipt is not present");
            return false;
        }
        TransactionReceipt receipt = receiptOpt.get();
        if (!receipt.getFrom().equalsIgnoreCase(expectedFrom)) {
            logger.error("validate failed: expected from is " + expectedFrom + ", actual is " + receipt.getFrom());
            return false;
        }
        if (!receipt.getTo().equalsIgnoreCase(expectedTo)) {
            logger.error("validate failed: expected to is " + expectedTo + ", actual is " + receipt.getTo());
            return false;
        }
        if (!receipt.isStatusOK()) {
            logger.error("validate failed: receipt status is not OK");
            return false;
        }

        EthTransaction transactionResp = null;
        try {
            transactionResp = ethWeb3j.ethGetTransactionByHash(transactionHash).sendAsync().get();
        } catch (Exception e) {
            logger.error("validate failed: getTransactionByHash exception: ", e);
            return false;
        }
        logger.debug("transactionResp: " + transactionResp);
        if (transactionResp.hasError()) {
            logger.error("validate failed: getTransactionByHash has error.");
            return false;
        }
        Optional<Transaction> transactionOpt = transactionResp.getTransaction();
        if (!transactionOpt.isPresent()) {
            logger.error("validate failed: get transaction by hash is not present.");
            return false;
        }
        Transaction transaction = transactionOpt.get();
        BigDecimal actualAmount = new BigDecimal(transaction.getValue());
        if (actualAmount.compareTo(expectedAmount.scaleByPowerOfTen(18)) != 0) {
            logger.error("validate failed: expected amount is " + expectedAmount.scaleByPowerOfTen(18) + ", actual is " + actualAmount);
            return false;
        }
        return true;
    }

    public boolean validateBtcTransaction() {
        // TODO:
        return true;
    }
}
