package com.dockey.users.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Configuration for Keycloak Admin API access
 */
@ApplicationScoped
public class KeycloakConfig {

    @Inject
    @ConfigProperty(name = "keycloak.auth-server-url", defaultValue = "http://localhost:8180")
    private String authServerUrl;

    @Inject
    @ConfigProperty(name = "keycloak.realm", defaultValue = "dockey")
    private String realm;

    @Inject
    @ConfigProperty(name = "keycloak.admin.username", defaultValue = "admin")
    private String adminUsername;

    @Inject
    @ConfigProperty(name = "keycloak.admin.password", defaultValue = "admin")
    private String adminPassword;

    public String getAuthServerUrl() {
        return authServerUrl;
    }

    public String getRealm() {
        return realm;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }
}
