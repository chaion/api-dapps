package com.chaion.makkiiserver.blockchain;

import com.chaion.makkiiserver.model.EthToken;
import com.chaion.makkiiserver.pokket.model.TransactionStatus;
import com.chaion.makkiiserver.pokket.model.TransactionStatusListener;
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
    /**
     * pending transaction list. scheduled task will check transactions' status.
     */
    private Map<String, TransactionStatus> pendingTransactions = new ConcurrentHashMap<>();

    public BlockchainService(@Value("${blockchain.eth.rpcserver}") String rpcServer) {
        logger.info("initialize blockchain service: " + rpcServer);
        ethWeb3j = Web3j.build(new HttpService(rpcServer));
    }

    /**
     * send raw transaction
     *
     * @param rawTx
     * @return
     * @throws BlockchainException
     */
    public String sendRawTransaction(String rawTx) throws BlockchainException {
        EthSendTransaction sendTxResp = null;
        logger.info("[eth][sendRawTransaction] raw: " + rawTx);
        try {
            sendTxResp = ethWeb3j.ethSendRawTransaction(rawTx).sendAsync().get();
            if (sendTxResp.hasError()) {
                Response.Error error = sendTxResp.getError();
                logger.error("[eth][sendRawTransaction] failed: ", error);
                throw new BlockchainException(error.getCode() + " " + error.getMessage());
            }
            String txHash = sendTxResp.getTransactionHash();
            logger.info("[eth][sendRawTransaction] txHash=" + txHash);
            return txHash;
        } catch (Exception e) {
            logger.error("[eth][sendRawTransaction] exception:" + e.getMessage());
            throw new BlockchainException(e.getMessage());
        }
    }

    /**
     * add transaction to pending transaction list.
     *
     * @param transactionHash
     * @param listener
     */
    public void addPendingTransaction(String transactionHash, TransactionStatusListener listener) {
        pendingTransactions.put(transactionHash, new TransactionStatus(transactionHash, listener));
    }

    public void checkPendingTxStatus() {
        logger.info("check pending tx status...");
        for (Map.Entry<String, TransactionStatus> entry : pendingTransactions.entrySet()) {
            TransactionStatus st = entry.getValue();
            String txHash = st.getTxHash();
            logger.info("processing tx: " + txHash + " " + st.getStatus());
            if (st.getStatus().equalsIgnoreCase(TransactionStatus.WAIT_RECEIPT)) {
                checkTxStatusOfReceipt(st, txHash);
            } else if (st.getStatus().equalsIgnoreCase(TransactionStatus.WAIT_BLOCK_NUMBER)) {
                checkTxStatusOfBlockNumber(st, txHash);
            }
        }
    }

    private void checkTxStatusOfBlockNumber(TransactionStatus st, String txHash) {
        EthBlockNumber blockNumberResp = null;
        try {
            blockNumberResp = ethWeb3j.ethBlockNumber().sendAsync().get();
        } catch (Exception e) {
            logger.debug("[eth][blockNumber] txHash=" + txHash + " exception: ", e);
            return;
        }
        if (blockNumberResp.hasError()) {
            logger.debug("[eth][blockNumber] txHash=" + txHash + ", error: " + blockNumberResp.getError());
            return;
        }
        BigInteger blockNumber = blockNumberResp.getBlockNumber();
        logger.info("tx[" + txHash + "] tx block number=" + st.getBlockNumber() + ", latest block number=" + blockNumber);
        if (blockNumber.subtract(st.getBlockNumber()).longValue() > 6) {
            st.setStatus(TransactionStatus.CONFIRMED);
            st.getListener().transactionConfirmed(txHash, true);
            pendingTransactions.remove(txHash);
        }
    }

    private void checkTxStatusOfReceipt(TransactionStatus st, String txHash) {
        EthGetTransactionReceipt receiptResp = null;
        try {
            receiptResp = ethWeb3j.ethGetTransactionReceipt(txHash).sendAsync().get();
        } catch (Exception e) {
            logger.error("[eth][getTransactionReceipt] txHash=" + txHash + " exception: ", e);
            return;
        }
        if (receiptResp.hasError()) {
            logger.debug("[eth][getTransactionReceipt] txHash=" + txHash + ", error: " + receiptResp.getError());
            return;
        }
        Optional<TransactionReceipt> receiptOpt = receiptResp.getTransactionReceipt();
        if (!receiptOpt.isPresent()) {
            logger.debug("[eth][getTransactionReceipt] txHash=" + txHash + " receipt is not present");
            return;
        }
        TransactionReceipt receipt = receiptOpt.get();
        if (receipt.isStatusOK()) {
            logger.info("tx[" + txHash + "] confirmed ok, block number="
                    + receipt.getBlockNumber() + ". wait block confirmation");
            st.setBlockNumber(receipt.getBlockNumber());
            st.setStatus(TransactionStatus.WAIT_BLOCK_NUMBER);
        } else {
            logger.info("tx[" + txHash + "] receipt is confirmed fail");
            st.getListener().transactionConfirmed(txHash, false);
            pendingTransactions.remove(txHash);
        }
    }

    public boolean validateERC20Transaction(String transactionHash,
                                            String expectedFrom, String expectedTo,
                                            String token,
                                            BigDecimal expectedAmount) {
        logger.info("validate erc20 transfer transaction[transactionHash=" + transactionHash +
                ",from=" + expectedFrom +
                ",to=" + expectedTo +
                ",token=" + token +
                ",amount=" + expectedAmount + "]");
        EthGetTransactionReceipt receiptResp = null;
        try {
            receiptResp = ethWeb3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();
        } catch (Exception e) {
            logger.error("validate erc20 failed: getTransactionReceipt", e);
            return false;
        }
        if (receiptResp.hasError()) {
            logger.error("validate erc20 failed: getTransactionReceipt has error", receiptResp.getError());
            return false;
        }
        Optional<TransactionReceipt> receiptOpt = receiptResp.getTransactionReceipt();
        if (!receiptOpt.isPresent()) {
            logger.error("validate erc20 failed: getTransactionReceipt is not present");
            return false;
        }
        TransactionReceipt receipt = receiptOpt.get();
        if (!receipt.getFrom().equalsIgnoreCase(expectedFrom)) {
            logger.error("validate erc20 failed: from address is different: " + receipt.getFrom());
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
            logger.error("validate erc20 failed: " + receipt.getTo() + " is not " + token + "'s contract address.");
            return false;
        }
        if (!receipt.isStatusOK()) {
            logger.error("validate erc20 failed: receipt status is not ok");
            return false;
        }

        EthTransaction transactionResp = null;
        try {
            transactionResp = ethWeb3j.ethGetTransactionByHash(transactionHash).sendAsync().get();
        } catch (Exception e) {
            logger.error("validate erc20 failed: getTransactionByHash failed.", e);
            return false;
        }
        if (transactionResp.hasError()) {
            logger.error("validate erc20 failed: getTransactionByHash has error", transactionResp.getError());
            return false;
        }
        Optional<Transaction> transactionOpt = transactionResp.getTransaction();
        if (!transactionOpt.isPresent()) {
            logger.error("validate erc20 failed: getTransaction is not present.");
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
                logger.error("validate failed: to address is different: " + address.getValue());
                return false;
            }
            BigInteger expectedAmountBI = expectedAmount.scaleByPowerOfTen(18).toBigInteger();
            if (amount.getValue().compareTo(expectedAmountBI) != 0) {
                logger.error("validate failed: expected amount is " + expectedAmountBI + ", actual amount is" + amount.getValue());
                return false;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("validated failed: decode input data failed");
            return false;
        }
        return true;
    }

    public boolean validateEthTx(String transactionHash, String expectedFrom, String expectedTo, BigDecimal expectedAmount) {
        logger.info("validate eth transfer transaction[transactionHash=" + transactionHash +
                ", from=" + expectedFrom +
                ", to=" + expectedTo +
                ", amount=" + expectedAmount + "]");
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
            logger.error("validate failed: getTransactionReceipt is not present");
            return false;
        }
        TransactionReceipt receipt = receiptOpt.get();
        if (!receipt.getFrom().equalsIgnoreCase(expectedFrom)) {
            logger.error("validate failed: from address is different: " + receipt.getFrom());
            return false;
        }
        if (!receipt.getTo().equalsIgnoreCase(expectedTo)) {
            logger.error("validate failed: to address is different: " + receipt.getTo());
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
        if (transactionResp.hasError()) {
            logger.error("validate failed: getTransactionByHash has error.");
            return false;
        }
        Optional<Transaction> transactionOpt = transactionResp.getTransaction();
        if (!transactionOpt.isPresent()) {
            logger.error("validate failed: getTransactionByHash is not present.");
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
