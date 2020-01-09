package com.chaion.makkiiserver.blockchain.eth;

import com.chaion.makkiiserver.blockchain.BaseBlockchain;
import com.chaion.makkiiserver.blockchain.BlockchainException;
import com.chaion.makkiiserver.blockchain.TransactionConfirmEvent;
import com.chaion.makkiiserver.modules.blockchain.token.EthToken;
import com.chaion.makkiiserver.blockchain.TransactionStatus;
import com.chaion.makkiiserver.modules.blockchain.token.EthTokenRepository;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeDecoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.*;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Service
public class EthService extends BaseBlockchain {

    private static final Logger logger = LoggerFactory.getLogger(EthService.class);

    public static final String POKKET_CHAIN = "PokketChain";
    public static final String ETHEREUM_CHAIN = "Ethereum";

    @Autowired
    EventBus eventBus;

    @Autowired
    EthTokenRepository ethTokenRepo;

    @Autowired
    PokketEthTokenProvider pokketEthTokenProvider;

    @Value("${app_env}")
    String appEnv;

    private Web3j ethWeb3j;

    public EthService(@Value("${blockchain.eth.apiinterface}") String rpcServerInterface,
                      @Value("${blockchain.eth.apiserver}") String rpcServer) {
        logger.info("initialize blockchain service: " + rpcServerInterface + ":" + rpcServer);
        if (rpcServerInterface.equalsIgnoreCase("ipc")) {
            ethWeb3j = AlethWeb3j.build(new UnixIpcService(rpcServer));
        } else {
            ethWeb3j = Web3j.build(new HttpService(rpcServer));
        }
    }

    @Override
    public String getName() {
        if (appEnv.equalsIgnoreCase("pokket")) {
            return POKKET_CHAIN;
        } else {
            return ETHEREUM_CHAIN;
        }
    }

    // ------------------------ pure jsonrpc -------------------------
    public BigInteger getTransactionCount(String address) throws BlockchainException {
        EthGetTransactionCount resp;
        try {
            resp = ethWeb3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).sendAsync().get();
        } catch (Exception e) {
            throw new BlockchainException(e.getMessage());
        }
        return resp.getTransactionCount();
    }
    public BigInteger getBalance(String address) throws BlockchainException {
        EthGetBalance resp = null;
        try {
            resp = ethWeb3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get();
        } catch (Exception e) {
            throw new BlockchainException(e.getMessage());
        }
        return resp.getBalance();
    }

    public BigInteger blockNumber() throws BlockchainException {
        EthBlockNumber blockNumberResp = null;
        try {
            blockNumberResp = ethWeb3j.ethBlockNumber().sendAsync().get();
        } catch (Exception e) {
            throw new BlockchainException(e.getMessage());
        }
        if (blockNumberResp.hasError()) {
            throw new BlockchainException(blockNumberResp.getError().toString());
        }
        return blockNumberResp.getBlockNumber();
    }

    public EthBlock.Block getBlockByNumber(BigInteger blockNumber) throws BlockchainException {
        EthBlock ethBlockResp = null;
        try {
            ethBlockResp = ethWeb3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNumber), false)
                    .sendAsync().get();
        } catch (Exception e) {
            throw new BlockchainException(e.getMessage());
        }
        if (ethBlockResp.hasError()) {
            throw new BlockchainException(ethBlockResp.getError().toString());
        }
        EthBlock.Block block = ethBlockResp.getBlock();
        return block;
    }

    public Transaction getTransaction(String txHash) throws BlockchainException {
        EthTransaction resp = null;
        try {
            resp = ethWeb3j.ethGetTransactionByHash(txHash).sendAsync().get();
        } catch (Exception e) {
            throw new BlockchainException(e.getMessage());
        }
        if (resp.hasError()) {
            throw new BlockchainException(resp.getError().toString());
        }
        Optional<Transaction> transactionOpt = resp.getTransaction();
        if (!transactionOpt.isPresent()) {
            throw new BlockchainException("transaction is not present.");
        }
        return transactionOpt.get();
    }

    public TransactionReceipt getTransactionReceipt(String txHash) throws BlockchainException {
        if (ethWeb3j instanceof AlethJsonRpc2_0Web3j) {
            AlethJsonRpc2_0Web3j aw = (AlethJsonRpc2_0Web3j) ethWeb3j;
            AlethEthGetTransactionReceipt resp = null;
            try {
                resp = aw.alethEthGetTransactionReceipt(txHash).sendAsync().get();
            } catch (Exception e) {
                throw new BlockchainException(e.getMessage());
            }
            if (resp.hasError()) {
                throw new BlockchainException(resp.getError().toString());
            }
            Optional<PlainTransactionReceipt> receiptOpt = resp.getTransactionReceipt();
            if (!receiptOpt.isPresent()) {
                return null;
            }
            return receiptOpt.get();
        } else {
            EthGetTransactionReceipt resp = null;
            try {
                resp = ethWeb3j.ethGetTransactionReceipt(txHash).sendAsync().get();
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
            logger.info("[eth][sendRawTransaction] txId=" + txHash);
            return txHash;
        } catch (Exception e) {
            logger.error("[eth][sendRawTransaction] exception:" + e.getMessage());
            throw new BlockchainException(e.getMessage());
        }
    }

    public String call(org.web3j.protocol.core.methods.request.Transaction tx, DefaultBlockParameter param) throws BlockchainException {
        EthCall resp = null;

        try {
            resp = ethWeb3j.ethCall(tx, param).sendAsync().get();
            if (resp.hasError()) {
                Response.Error error = resp.getError();
                throw new BlockchainException(error.toString());
            }
            return resp.getValue();
        } catch (Exception e) {
            throw new BlockchainException(e.getMessage());
        }

    }

    public void checkPendingTxStatus(TransactionStatus st) {
//        logger.info("check pending eth tx status...");
        String txHash = st.getTxId();
        logger.info("processing tx: " + txHash + " " + st.getStatus());
        if (st.getStatus().equalsIgnoreCase(TransactionStatus.WAIT_RECEIPT)) {
            checkTxStatusOfReceipt(st, txHash);
        } else if (st.getStatus().equalsIgnoreCase(TransactionStatus.WAIT_BLOCK_NUMBER)) {
            checkTxStatusOfBlockNumber(st, txHash);
        }
    }

    private void checkTxStatusOfBlockNumber(TransactionStatus st, String txHash) {
        EthBlockNumber blockNumberResp = null;
        try {
            blockNumberResp = ethWeb3j.ethBlockNumber().sendAsync().get();
        } catch (Exception e) {
            logger.debug("[eth][blockNumber] txId=" + txHash + " exception: ", e);
            return;
        }
        if (blockNumberResp.hasError()) {
            logger.debug("[eth][blockNumber] txId=" + txHash + ", error: " + blockNumberResp.getError());
            return;
        }
        BigInteger blockNumber = blockNumberResp.getBlockNumber();
        logger.info("tx[" + txHash + "] tx block number=" + st.getBlockNumber() + ", latest block number=" + blockNumber);
        if (blockNumber.subtract(st.getBlockNumber()).longValue() > 6) {
            st.setStatus(TransactionStatus.CONFIRMED);
            txStatusRepo.save(st);
            logger.info("send transaction confirm event: " + st);
            eventBus.post(new TransactionConfirmEvent(st));
        }
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
        if (receipt.isStatusOK()) {
            if (receipt instanceof PlainTransactionReceipt) {
                logger.info("tx[" + txHash + "] confirmed ok, block number="
                        + ((PlainTransactionReceipt) receipt).getBlockNumberString() + ". wait block confirmation");
                st.setBlockNumber(new BigInteger(((PlainTransactionReceipt) receipt).getBlockNumberString()));
            } else {
                logger.info("tx[" + txHash + "] confirmed ok, block number="
                        + receipt.getBlockNumber() + ". wait block confirmation");
                st.setBlockNumber(receipt.getBlockNumber());
            }
            st.setStatus(TransactionStatus.WAIT_BLOCK_NUMBER);
            st.setResult(true);
            txStatusRepo.save(st);
        } else {
            logger.info("tx[" + txHash + "] receipt is confirmed fail");
            st.setStatus(TransactionStatus.CONFIRMED);
            st.setResult(false);
            txStatusRepo.save(st);
            eventBus.post(new TransactionConfirmEvent(st));
        }
    }

    /**
     * Validate transaction details and get actual transaction amount
     *
     * @param transactionHash
     * @param expectedFrom
     * @param expectedTo
     * @param token
     * @param expectedAmount
     * @param amountValidator
     * @return
     */
    public BigInteger validateERC20Transaction(String transactionHash,
                                            String expectedFrom,
                                            String expectedTo,
                                            String token,
                                            BigDecimal expectedAmount,
                                            BiFunction<BigInteger, BigInteger, Boolean> amountValidator)
            throws BlockchainException {
        logger.info("validate erc20 transfer transaction[transactionHash=" + transactionHash +
                ",from=" + expectedFrom +
                ",to=" + expectedTo +
                ",token=" + token +
                ",amount=" + expectedAmount + "]");
        EthGetTransactionReceipt receiptResp = null;
        try {
            receiptResp = ethWeb3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();
        } catch (Exception e) {
            throw new BlockchainException("validate erc20 failed: getTransactionReceipt: " + e.getMessage());
        }
        if (receiptResp.hasError()) {
            throw new BlockchainException("validate erc20 failed: getTransactionReceipt has error: " + receiptResp.getError().toString());
        }
        Optional<TransactionReceipt> receiptOpt = receiptResp.getTransactionReceipt();
        if (!receiptOpt.isPresent()) {
            throw new BlockchainException("validate erc20 failed: getTransactionReceipt is not present");
        }
        TransactionReceipt receipt = receiptOpt.get();
        if (expectedFrom != null && !receipt.getFrom().equalsIgnoreCase(expectedFrom)) {
            throw new BlockchainException("validate erc20 failed: from address is different: " + receipt.getFrom());
        }
        List<EthToken> ethTokens = null;
        if (appEnv.equalsIgnoreCase("pokket")) {
            ethTokens = pokketEthTokenProvider.getBySymbol(token);
        } else {
            ethTokens = ethTokenRepo.findBySymbol(token);
        }
        boolean isToAddressCorrect = false;
        for (EthToken ethToken : ethTokens) {
            if (ethToken.getContractAddr().equalsIgnoreCase(receipt.getTo())) {
                isToAddressCorrect = true;
                break;
            }
        }
        if (!isToAddressCorrect) {
            throw new BlockchainException("validate erc20 failed: " + receipt.getTo() + " is not " + token + "'s contract address.");
        }
        if (!receipt.isStatusOK()) {
            throw new BlockchainException("validate erc20 failed: receipt status is not ok");
        }

        EthTransaction transactionResp = null;
        try {
            transactionResp = ethWeb3j.ethGetTransactionByHash(transactionHash).sendAsync().get();
        } catch (Exception e) {
            throw new BlockchainException("validate erc20 failed: getTransactionByHash failed." + e.getMessage());
        }
        if (transactionResp.hasError()) {
            throw new BlockchainException("validate erc20 failed: getTransactionByHash has error" + transactionResp.getError().toString());
        }
        Optional<Transaction> transactionOpt = transactionResp.getTransaction();
        if (!transactionOpt.isPresent()) {
            throw new BlockchainException("validate erc20 failed: getTransaction is not present.");
        }
        Transaction transaction = transactionOpt.get();
        String input = transaction.getInput();
        try {
            // decode input data
            String method = input.substring(0, 10);
            String to = input.substring(10, 74);
            String value = input.substring(74);
            Method refMethod = null;
            refMethod = TypeDecoder.class.getDeclaredMethod("decode", String.class, int.class, Class.class);
            refMethod.setAccessible(true);
            Address address = (Address) refMethod.invoke(null, to, 0, Address.class);
            Uint256 amount = (Uint256) refMethod.invoke(null, value, 0, Uint256.class);
            // validate amount and to in input
            if (expectedTo != null && !address.getValue().equalsIgnoreCase(expectedTo)) {
                throw new BlockchainException("validate failed: to address is different: " + address.getValue());
            }
            BigInteger expectedAmountBI = expectedAmount.scaleByPowerOfTen(18).toBigInteger();
            if (!(amountValidator != null && amountValidator.apply(amount.getValue(), expectedAmountBI)) ||
                    (amountValidator == null && amount.getValue().compareTo(expectedAmountBI) != 0)) {
                throw new BlockchainException("validate failed: expected amount is " + expectedAmountBI + ", actual amount is" + amount.getValue());
            }
            return amount.getValue();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new BlockchainException("validated failed: decode input data failed");
        }
    }

    public BigInteger validateEthTx(String transactionHash,
                                 String expectedFrom,
                                 String expectedTo,
                                 BigDecimal expectedAmount,
                                 BiFunction<BigInteger, BigInteger, Boolean> amountValidator) throws BlockchainException {
        logger.info("validate eth transfer transaction[transactionHash=" + transactionHash +
                ", from=" + expectedFrom +
                ", to=" + expectedTo +
                ", amount=" + expectedAmount + "]");
        EthGetTransactionReceipt receiptResp = null;
        try {
            receiptResp = ethWeb3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();
        } catch (Exception e) {
            throw new BlockchainException("validate failed: getTransactionReceipt exception: " + e.getMessage());
        }
        logger.debug("receiptResp: " + receiptResp);
        if (receiptResp.hasError()) {
            throw new BlockchainException("validate failed: getTransactionReceipt has error");
        }
        Optional<TransactionReceipt> receiptOpt = receiptResp.getTransactionReceipt();
        if (!receiptOpt.isPresent()) {
            throw new BlockchainException("validate failed: getTransactionReceipt is not present");
        }
        TransactionReceipt receipt = receiptOpt.get();
        if (expectedFrom != null && !receipt.getFrom().equalsIgnoreCase(expectedFrom)) {
            throw new BlockchainException("validate failed: from address is different: " + receipt.getFrom());
        }
        if (!receipt.getTo().equalsIgnoreCase(expectedTo)) {
            throw new BlockchainException("validate failed: to address is different: " + receipt.getTo());
        }
        if (!receipt.isStatusOK()) {
            throw new BlockchainException("validate failed: receipt status is not OK");
        }

        EthTransaction transactionResp = null;
        try {
            transactionResp = ethWeb3j.ethGetTransactionByHash(transactionHash).sendAsync().get();
        } catch (Exception e) {
            throw new BlockchainException("validate failed: getTransactionByHash exception: " + e.getMessage());
        }
        if (transactionResp.hasError()) {
            throw new BlockchainException("validate failed: getTransactionByHash has error.");
        }
        Optional<Transaction> transactionOpt = transactionResp.getTransaction();
        if (!transactionOpt.isPresent()) {
            throw new BlockchainException("validate failed: getTransactionByHash is not present.");
        }
        Transaction transaction = transactionOpt.get();
        BigInteger actualAmount = transaction.getValue();
        BigInteger expectedAmountBI = expectedAmount.scaleByPowerOfTen(18).toBigInteger();
        if (!(amountValidator != null && amountValidator.apply(actualAmount, expectedAmountBI)) ||
                (amountValidator == null && actualAmount.compareTo(expectedAmountBI) != 0)) {
            throw new BlockchainException("validate failed: expected amount is " + expectedAmount.scaleByPowerOfTen(18) + ", actual is " + actualAmount);
        }
        return actualAmount;
    }

    /**
     * only for debug purpose as there is no open http jsonrpc channel for aleth ipc
     */
    public String debugEthTx(String transactionHash) {
        StringBuilder sb = new StringBuilder();
        sb.append("debug eth transaction: transactionHash=" + transactionHash + "\n");
        EthGetTransactionReceipt receiptResp = null;
        try {
            receiptResp = ethWeb3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();
        } catch (Exception e) {
            sb.append("getTransactionReceipt exception: " + e.getMessage() + "\n");
            return sb.toString();
        }
        sb.append("receiptResp: " + receiptResp + "\n");
        if (receiptResp.hasError()) {
            sb.append("getTransactionReceipt has error\n");
            return sb.toString();
        }
        Optional<TransactionReceipt> receiptOpt = receiptResp.getTransactionReceipt();
        if (!receiptOpt.isPresent()) {
            sb.append("getTransactionReceipt is not present\n");
            return sb.toString();
        }
        TransactionReceipt receipt = receiptOpt.get();
        sb.append("from:" + receipt.getFrom() + "\n");
        sb.append("to:" + receipt.getTo() + "\n");
        sb.append("status:" + receipt.isStatusOK() + "\n");

        EthTransaction transactionResp = null;
        try {
            transactionResp = ethWeb3j.ethGetTransactionByHash(transactionHash).sendAsync().get();
        } catch (Exception e) {
            sb.append("getTransactionByHash exception: " + e.getMessage() + "\n");
            return sb.toString();
        }
        if (transactionResp.hasError()) {
            sb.append("getTransactionByHash has error.\n");
            return sb.toString();
        }
        Optional<Transaction> transactionOpt = transactionResp.getTransaction();
        if (!transactionOpt.isPresent()) {
            sb.append("getTransactionByHash is not present.\n");
            return sb.toString();
        }
        Transaction transaction = transactionOpt.get();
        sb.append("value: " + transaction.getValue() + "\n");
        return sb.toString();
    }

    /**
     * only for debug purpose as there is no open http jsonrpc channel for aleth ipc
     */
    public String debugErc20Tx(String transactionHash) {
        StringBuilder sb = new StringBuilder();
        sb.append("debug erc20 transaction:transactionHash=" + transactionHash + "\n");
        EthGetTransactionReceipt receiptResp = null;
        try {
            receiptResp = ethWeb3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();
        } catch (Exception e) {
            sb.append("getTransactionReceipt exception: " + e.getMessage() + "\n");
            return sb.toString();
        }
        sb.append("receiptResp: " + receiptResp + "\n");
        if (receiptResp.hasError()) {
            sb.append("getTransactionReceipt has error\n");
            return sb.toString();
        }
        Optional<TransactionReceipt> receiptOpt = receiptResp.getTransactionReceipt();
        if (!receiptOpt.isPresent()) {
            sb.append("getTransactionReceipt is not present\n");
            return sb.toString();
        }
        TransactionReceipt receipt = receiptOpt.get();
        sb.append("from:" + receipt.getFrom() + "\n");
        sb.append("to:" + receipt.getTo() + "\n");
        sb.append("status:" + receipt.isStatusOK() + "\n");

        EthTransaction transactionResp = null;
        try {
            transactionResp = ethWeb3j.ethGetTransactionByHash(transactionHash).sendAsync().get();
        } catch (Exception e) {
            sb.append("getTransactionByHash exception: " + e + "\n");
            return sb.toString();
        }
        if (transactionResp.hasError()) {
            sb.append("getTransactionByHash has error.\n");
            return sb.toString();
        }
        Optional<Transaction> transactionOpt = transactionResp.getTransaction();
        if (!transactionOpt.isPresent()) {
            sb.append("getTransactionByHash is not present.\n");
            return sb.toString();
        }
        Transaction transaction = transactionOpt.get();
        String input = transaction.getInput();
        sb.append("input:" + input + "\n");
        try {
            // decode input data
            String method = input.substring(0, 10);
            sb.append("method: " + method + "\n");
            String to = input.substring(10, 74);
            sb.append("erc20 raw to:" + to + "\n");
            String value = input.substring(74);
            sb.append("erc20 raw value:" + value + "\n");
            Method refMethod = null;
            refMethod = TypeDecoder.class.getDeclaredMethod("decode", String.class, int.class, Class.class);
            refMethod.setAccessible(true);
            Address address = (Address) refMethod.invoke(null, to, 0, Address.class);
            Uint256 amount = (Uint256) refMethod.invoke(null, value, 0, Uint256.class);
            sb.append("erc20 to: " + address.getValue() + "\n");
            sb.append("erc20 value: " + amount.getValue() + "\n");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            sb.append("decode input data failed\n");
        }
        return sb.toString();
    }

}
