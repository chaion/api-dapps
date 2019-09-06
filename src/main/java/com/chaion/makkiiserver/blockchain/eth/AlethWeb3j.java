package com.chaion.makkiiserver.blockchain.eth;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;

import java.util.concurrent.ScheduledExecutorService;

public interface AlethWeb3j extends Web3j {
    static Web3j build(Web3jService web3jService) {
        return new AlethJsonRpc2_0Web3j(web3jService);
    }

    static Web3j build(Web3jService web3jService, long pollingInterval, ScheduledExecutorService scheduledExecutorService) {
        return new AlethJsonRpc2_0Web3j(web3jService, pollingInterval, scheduledExecutorService);
    }
}
