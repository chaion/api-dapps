package com.chaion.makkiiserver.repository;

import com.chaion.makkiiserver.model.ATSToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AionTokenRepository extends MongoRepository<ATSToken, String> {
    @Query("{'$or': [{'name': {$regex: '?0', $options: 'i'}}, {'symbol': {$regex: '?0', $options: 'i'}}]}")
    List<ATSToken> findByName(String name);

    @Query("{'contractAddress': '?0'}")
    List<ATSToken> findByContractAddress(String address);
}
