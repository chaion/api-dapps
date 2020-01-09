package com.chaion.makkiiserver.blockchain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionStatusRepository extends MongoRepository<TransactionStatus, String> {

    List<TransactionStatus> findByChainNameAndStatusIn(String chainName, List<String> statusIn);

}
