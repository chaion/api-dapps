package com.chaion.makkiiserver.blockchain;

import java.util.Map;

public interface BlockchainService {

    String getName();

    String sendRawTransaction(String rawTransaction) throws BlockchainException;

    void addPendingTransaction(String transactionId, Map<String, Object> customData);

}
