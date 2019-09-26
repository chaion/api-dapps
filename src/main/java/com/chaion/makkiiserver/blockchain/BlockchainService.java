package com.chaion.makkiiserver.blockchain;

public interface BlockchainService {

    String sendRawTransaction(String rawTransaction) throws BlockchainException;

}
