package com.chaion.makkiiserver.modules.blockchain.transaction;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends MongoRepository<SimpleTransaction, String> {
    SimpleTransaction findFirstByChainAndTxHash(String chain, String txHash);
}
