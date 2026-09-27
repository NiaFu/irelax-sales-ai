package com.irelax.salesai.auth;

import com.irelax.salesai.config.AppProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class OwnerContext {
    private final AppProperties properties;

    public OwnerContext(AppProperties properties) {
        this.properties = properties;
    }

    public String currentOwnerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwt && jwt.isAuthenticated()) {
            return jwt.getToken().getSubject();
        }
        return defaultOwnerId();
    }

    public String webhookOwnerId() {
        if (properties.twilio() != null && properties.twilio().ownerId() != null && !properties.twilio().ownerId().isBlank()) {
            return properties.twilio().ownerId();
        }
        return defaultOwnerId();
    }

    private String defaultOwnerId() {
        if (properties.auth() != null && properties.auth().devOwnerId() != null && !properties.auth().devOwnerId().isBlank()) {
            return properties.auth().devOwnerId();
        }
        return "nia";
    }
}
