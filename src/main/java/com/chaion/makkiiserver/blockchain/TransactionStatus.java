package com.chaion.makkiiserver.blockchain;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigInteger;
import java.util.Map;

@Data
public class TransactionStatus {

    public static final String KEY_DOMAIN = "domain";
    private static final long EXPIRATION = 10 * 60 * 1000;
    public static final String WAIT_RECEIPT = "wait_for_receipt";
    public static final String WAIT_BLOCK_NUMBER = "wait_for_block_number";
    public static final String CONFIRMED = "confirmed";
    public static final String TIMEOUT = "timeout";

    @Id
    private String id;
    private String chainName;
    private Long currentTimestamp;
    private String txId;
    private BigInteger blockNumber;
    private String status;
    private boolean result;
    private Map<String, Object> customData;

    public TransactionStatus(String chainName, String txId, Map<String, Object> customData) {
        this.chainName = chainName;
        this.txId = txId;
        this.status = WAIT_RECEIPT;
        this.customData = customData;
        currentTimestamp = System.currentTimeMillis();
    }

    public boolean isExpire() {
        return System.currentTimeMillis() - this.currentTimestamp > EXPIRATION;
    }

    @Override
    public String toString() {
        return "TransactionStatus{" +
                "chainName='" + chainName + '\'' +
                ", txId='" + txId + '\'' +
                ", status='" + status + '\'' +
                ", result=" + result +
                '}';
    }
}
