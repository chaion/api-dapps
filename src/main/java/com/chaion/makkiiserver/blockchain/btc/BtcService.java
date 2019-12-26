package com.chaion.makkiiserver.blockchain.btc;

import com.chaion.makkiiserver.blockchain.BlockchainException;
import com.chaion.makkiiserver.blockchain.TransactionStatus;
import com.chaion.makkiiserver.blockchain.BaseBlockchain;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Service
public class BtcService extends BaseBlockchain {
    private static final Logger logger = LoggerFactory.getLogger(BtcService.class);

    @Autowired
    private RestTemplate restClient;

    private String baseurl;

    public BtcService(@Value("${blockchain.btc.apiserver}") String apiServer) {
        this.baseurl = apiServer;
    }

    public String sendRawTransaction(String rawTransaction) throws BlockchainException {
        String url = this.baseurl + "/tx/send";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> map = new HashMap<>();
        map.put("rawtx", rawTransaction);
        HttpEntity request = new HttpEntity(map, headers);
        try {
            ResponseEntity<String> response = restClient.postForEntity(url, request, String.class);
            logger.info("btc send raw tx resp: " + response.getBody());
            try {
                JsonObject root = new JsonParser().parse(response.getBody()).getAsJsonObject();
                if (root.has("txid")) {
                    return root.get("txid").getAsString();
                } else {
                    throw new BlockchainException("BTC sendRawTransaction invalid response format: missing txid property");
                }
            } catch (Exception e) {
                throw new BlockchainException("BTC sendRawTransaction invalid response format: " + e.getMessage());
            }
        } catch (HttpStatusCodeException e) {
            throw new BlockchainException("BTC sendRawTransaction exception: " +
                    e.getRawStatusCode() + ":" + e.getResponseBodyAsString());
        }
    }

    public BigInteger blockNumber() throws BlockchainException {
        String url = this.baseurl + "/api/status?q=getInfo";
        try {
            ResponseEntity<String> response = restClient.getForEntity(url, String.class);
            String body = response.getBody();
            JsonObject root = new JsonParser().parse(body).getAsJsonObject();
            JsonObject info = root.get("info").getAsJsonObject();
            return BigInteger.valueOf(info.get("blocks").getAsLong());
        } catch (HttpStatusCodeException e) {
            throw new BlockchainException("BTC blockNumber exception: " +
                    e.getRawStatusCode() + ":" + e.getResponseBodyAsString());
        }
    }

    public BtcTransaction getTransaction(String transactionId) throws BlockchainException {
        String url = this.baseurl + "/tx/" + transactionId;
        try {
            ResponseEntity<String> response = restClient.getForEntity(url, String.class);
            String body = response.getBody();
            JsonObject root = new JsonParser().parse(body).getAsJsonObject();
            return BtcTransaction.fromJson(root);
        } catch (HttpStatusCodeException e) {
            throw new BlockchainException("BTC getTransaction exception: " +
                    e.getRawStatusCode() + ":" + e.getResponseBodyAsString());
        }
    }

    public void checkPendingTxStatus() {
        logger.info("check pending btc tx status...");
        for (Map.Entry<String, TransactionStatus> entry : pendingTransactions.entrySet()) {
            TransactionStatus st = entry.getValue();
            String txId = st.getTxId();
            logger.info("processing tx: " + txId + " " + st.getStatus());
            BtcTransaction tx = null;
            try {
                tx = getTransaction(txId);
                if (st.getStatus().equalsIgnoreCase(TransactionStatus.WAIT_RECEIPT)) {
                    st.setStatus(TransactionStatus.WAIT_BLOCK_NUMBER);
                    st.setBlockNumber(tx.getBlockHeight());
                } else if (st.getStatus().equalsIgnoreCase(TransactionStatus.WAIT_BLOCK_NUMBER)) {
                    if (tx.getBlockHeight().subtract(st.getBlockNumber()).longValue() > 6) {
                        st.setStatus(TransactionStatus.CONFIRMED);
                        pendingTransactions.remove(txId);
                        st.getListener().transactionConfirmed(txId, true);
                    }
                }
            } catch (BlockchainException e) {
                logger.error("getTransaction of " + txId + " failed:" + e.getMessage() + ".");
                logger.info("skip tx " + txId);
            }
        }
    }

    public BigInteger validateBtcTransaction(String transactionId,
                                       String from,
                                       String to,
                                       BigDecimal expectedAmount,
                                       BiFunction<BigInteger, BigInteger, Boolean> amountValidator)
            throws BlockchainException {
        BtcTransaction transaction = null;
        try {
            transaction = getTransaction(transactionId);
        } catch (BlockchainException e) {
            throw new BlockchainException("validate btc transaction(" + transactionId + ") failed: " + e.getMessage());
        }

        List<BtcTxVin> vins = transaction.getVin();
        boolean hasFrom = false;
        for (BtcTxVin vin : vins) {
            if (vin.getAddr().equalsIgnoreCase(from)) {
                hasFrom = true;
                break;
            }
        }
        if (!hasFrom) {
            throw new BlockchainException("validate btc transaction(" + transactionId + ") failed: " +
                    " from addresses doesn't contain expected address " + from);
        }
        List<BtcTxVout> vouts = transaction.getVout();
        for (BtcTxVout vout : vouts) {
                boolean hasAddress = false;
                for (String address: vout.getScriptPubKey().getAddresses()) {
                    if (address.equalsIgnoreCase(to)) {
                        hasAddress = true;
                        break;
                    }
                }
                if (hasAddress) {
                    logger.info("validate btc amount: " + vout.getValue());
                    return new BigInteger(vout.getValue());
                }
        }
        throw new BlockchainException("validate btc transaction(" + transactionId + ") failed: " +
                "to address is missing or amount is different.");
    }
}
