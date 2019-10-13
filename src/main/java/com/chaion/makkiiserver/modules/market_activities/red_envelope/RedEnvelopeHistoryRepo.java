package com.chaion.makkiiserver.modules.market_activities.red_envelope;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedEnvelopeHistoryRepo extends MongoRepository<RedEnvelopeHistory, String> {

    int countByPhoneId(String phoneId);
    int countByAddress(String address);

}
