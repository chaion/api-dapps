package com.chaion.makkiiserver.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

/**
 * only resource server for all makkii resources.
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    private final String RESOURCE_ID_MAKKII = "makkii";

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId(RESOURCE_ID_MAKKII).stateless(true);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/image/**").permitAll()
                .antMatchers(
                        "/swagger*/**",
                        "/webjars/**",
                        "/v2/api-docs",
                        "/pokketchain",
                        "/pokket/**",
                        "/token/aion",
                        "/token/aion/search",
                        "/token/eth/search/",
                        "/token/eth/popular",
                        "/token/eth/token_name",
                        "/token/eth/img"
                ).permitAll()
                .anyRequest().authenticated();
    }
}
