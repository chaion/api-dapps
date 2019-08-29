package com.chaion.makkiiserver.blockchain;

import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.Request;

import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

public class AlethJsonRpc2_0Web3j extends JsonRpc2_0Web3j {
    public AlethJsonRpc2_0Web3j(Web3jService web3jService) {
        super(web3jService);
    }

    public AlethJsonRpc2_0Web3j(Web3jService web3jService, long pollingInterval, ScheduledExecutorService scheduledExecutorService) {
        super(web3jService, pollingInterval, scheduledExecutorService);
    }

    public Request<?, AlethEthGetTransactionReceipt> alethEthGetTransactionReceipt(String transactionHash) {
        return new Request("eth_getTransactionReceipt", Arrays.asList(transactionHash), this.web3jService, AlethEthGetTransactionReceipt.class);
    }
}
