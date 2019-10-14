package com.chaion.makkiiserver.modules.market_activities.red_envelope;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RedEnvelopeHistoryRepo extends MongoRepository<RedEnvelopeHistory, String> {

    int countByPhoneIdAndStatusIn(String phoneId, List<RedEnvelopeHsitoryStatus> in);
    int countByAddressAndStatusIn(String address, List<RedEnvelopeHsitoryStatus> in);
    RedEnvelopeHistory findFirstByTxHash(String txHash);

}
