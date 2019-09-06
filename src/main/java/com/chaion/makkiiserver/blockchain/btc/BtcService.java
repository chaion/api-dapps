package com.chaion.makkiiserver.blockchain.btc;

import com.chaion.makkiiserver.blockchain.TransactionStatus;
import com.chaion.makkiiserver.blockchain.BaseBlockchain;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BtcService extends BaseBlockchain {
    private static final Logger logger = LoggerFactory.getLogger(BtcService.class);

    @Autowired
    private RestTemplate restClient;

    private String baseurl;

    public BtcService(@Value("${blockchain.btc.apiserver}") String apiServer) {
        this.baseurl = apiServer;
    }

    public String sendRawTransaction(String rawTransaction) {
        String url = this.baseurl + "/tx/send";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> map = new HashMap<>();
        map.put("rawtx", rawTransaction);
        HttpEntity request = new HttpEntity(map, headers);
        ResponseEntity<String> response = restClient.postForEntity(url, request, String.class);
        if (response.getStatusCodeValue() == 200) {
            logger.info("btc send raw tx resp: " + response.getBody());
            return response.getBody();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, url);
        }
    }

    public BigInteger blockNumber() {
        String url = this.baseurl + "/api/status?q=getInfo";
        ResponseEntity<String> response = restClient.getForEntity(url, String.class);
        if (response.getStatusCodeValue() == 200) {
            String body = response.getBody();
            JsonObject root = new JsonParser().parse(body).getAsJsonObject();
            JsonObject info = root.get("info").getAsJsonObject();
            return BigInteger.valueOf(info.get("blocks").getAsLong());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, url);
        }
    }

    public BtcTransaction getTransaction(String transactionId) {
        String url = this.baseurl + "/tx/" + transactionId;
        ResponseEntity<String> response = restClient.getForEntity(url, String.class);
        if (response.getStatusCodeValue() == 200) {
            String body = response.getBody();
            System.out.println(body);
            JsonObject root = new JsonParser().parse(body).getAsJsonObject();
            return BtcTransaction.fromJson(root);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, url);
        }
    }

    public void checkPendingTxStatus() {
        logger.info("check pending btc tx status...");
        for (Map.Entry<String, TransactionStatus> entry : pendingTransactions.entrySet()) {
            TransactionStatus st = entry.getValue();
            String txId = st.getTxId();
            logger.info("processing tx: " + txId + " " + st.getStatus());
            BtcTransaction tx = getTransaction(txId);
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
        }
    }

    public boolean validateBtcTransaction(String transactionId, String from, String to, BigDecimal amount) {
        BtcTransaction transaction = getTransaction(transactionId);
        List<BtcTxVin> vins = transaction.getVin();
        boolean hasFrom = false;
        for (BtcTxVin vin : vins) {
            if (vin.getAddr().equalsIgnoreCase(from)) {
                hasFrom = true;
                break;
            }
        }
        if (!hasFrom) {
            logger.error("validate failed: from address is different");
            return false;
        }
        List<BtcTxVout> vouts = transaction.getVout();
        boolean hasToAndAmount = false;
        for (BtcTxVout vout : vouts) {
            if (new BigDecimal(vout.getValue()).equals(amount)) {
                boolean hasAddress = false;
                for (String address: vout.getScriptPubKey().getAddresses()) {
                    if (address.equalsIgnoreCase(to)) {
                        hasAddress = true;
                        break;
                    }
                }
                if (hasAddress) {
                    hasToAndAmount = true;
                    break;
                }
            }
        }
        if (!hasToAndAmount){
            logger.error("validate failed: to and amount is different");
            return false;
        }
        return true;
    }
}
