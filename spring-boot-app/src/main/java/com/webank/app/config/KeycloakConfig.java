package com.webank.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client.provider.keycloak")
@Data
public class KeycloakConfig {
    
    private String issuerUri;
    private String authorizationUri;
    private String tokenUri;
    private String userInfoUri;
    private String jwkSetUri;
    
    public String getIssuerUri() {
        return issuerUri != null ? issuerUri : "http://localhost:8180/realms/webank";
    }
    
    public String getTokenUri() {
        return tokenUri != null ? tokenUri : getIssuerUri() + "/protocol/openid-connect/token";
    }
    
    public String getUserInfoUri() {
        return userInfoUri != null ? userInfoUri : getIssuerUri() + "/protocol/openid-connect/userinfo";
    }
}
