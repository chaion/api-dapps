package com.chaion.makkiiserver.blockchain;

import com.chaion.makkiiserver.blockchain.eth.BlockchainException;

public interface BlockchainService {

    String sendRawTransaction(String rawTransaction) throws BlockchainException;

}
