package com.chaion.makkiiserver.security;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuthClientDetailsRepo extends MongoRepository<OAuthClientDetails, String> {
    OAuthClientDetails findByClientId(String clientId);
}
