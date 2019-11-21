package com.chaion.makkiiserver.security;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

import java.util.*;

@Data
public class OAuthClientDetails implements ClientDetails {
    @Id
    private String id;
    private String clientId;
    private Set<String> resourceIds;
    private String clientSecret;
    private Set<String> scope;
    private Set<String> authorizedGrantTypes;
    private Set<String> webServerRedirectUri;
    private Set<String> authoritiesSet;
    private Integer accessTokenValidity;
    private Integer refreshTokenValidity;
    private Map<String, Object> additionalInformation;
    private Set<String> autoapprove;

    @Override
    public boolean isSecretRequired() {
        return this.clientSecret != null;
    }

    @Override
    public boolean isScoped() {
        return this.scope != null && !this.scope.isEmpty();
    }

    @Override
    public Set<String> getRegisteredRedirectUri() {
        return this.webServerRedirectUri;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorityList = new ArrayList<>();
        for (String authority : authoritiesSet) {
            authorityList.add(new SimpleGrantedAuthority(authority));
        }
        return authorityList;
    }

    @Override
    public Integer getAccessTokenValiditySeconds() {
        return this.accessTokenValidity;
    }

    @Override
    public Integer getRefreshTokenValiditySeconds() {
        return this.refreshTokenValidity;
    }

    @Override
    public boolean isAutoApprove(String scope) {
        if (autoapprove == null) return false;
        Iterator ite = this.autoapprove.iterator();
        String auto;
        do {
            if (!ite.hasNext()) {
                return false;
            }
            auto = (String) ite.next();
        } while(!auto.equals("true") && !scope.matches(auto));
        return true;
    }
}
