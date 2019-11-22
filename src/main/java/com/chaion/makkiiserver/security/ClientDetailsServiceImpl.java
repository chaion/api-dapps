package com.chaion.makkiiserver.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

/**
 * Client details service, client information is maintained under in mongodb.
 */
public class ClientDetailsServiceImpl implements ClientDetailsService {

    @Autowired
    OAuthClientDetailsRepo repo;

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        return repo.findByClientId(clientId);
    }
}
