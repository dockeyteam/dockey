package com.dockey.users.security;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;

/**
 * Security filter to extract Keycloak user information
 */
@Provider
public class KeycloakSecurityFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        SecurityContext securityContext = requestContext.getSecurityContext();
        
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            Principal principal = securityContext.getUserPrincipal();
            // Keycloak principal is available here
            // You can extract user ID, roles, etc.
        }
    }
}
