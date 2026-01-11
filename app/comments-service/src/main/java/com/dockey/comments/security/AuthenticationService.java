package com.dockey.comments.security;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.security.Principal;
import java.util.Base64;
import java.util.Optional;

/**
 * Service to extract authenticated user information from JWT token
 */
@RequestScoped
public class AuthenticationService {

    @Inject
    private HttpServletRequest httpServletRequest;

    /**
     * Parse JWT token from Authorization header
     */
    private Optional<JsonObject> parseJwtToken() {
        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }

        try {
            String token = authHeader.substring(7);
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return Optional.empty();
            }

            // Decode payload (second part of JWT)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonReader jsonReader = Json.createReader(new StringReader(payload));
            JsonObject jsonObject = jsonReader.readObject();
            return Optional.of(jsonObject);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Get the Keycloak user ID (subject) from the JWT token
     */
    public Optional<String> getKeycloakUserId() {
        return parseJwtToken()
                .map(token -> token.getString("sub", null));
    }

    /**
     * Get the username from JWT token
     */
    public Optional<String> getUsername() {
        // Try preferred_username from token
        Optional<String> preferred = parseJwtToken()
                .map(token -> token.getString("preferred_username", null));
        
        if (preferred.isPresent()) {
            return preferred;
        }

        // Fallback to principal name
        Principal principal = httpServletRequest.getUserPrincipal();
        if (principal != null) {
            return Optional.of(principal.getName());
        }
        
        return Optional.empty();
    }

    /**
     * Get the email from JWT token
     */
    public Optional<String> getEmail() {
        return parseJwtToken()
                .map(token -> token.getString("email", null));
    }

    /**
     * Get the full name from JWT token
     */
    public Optional<String> getFullName() {
        return parseJwtToken()
                .map(token -> {
                    String givenName = token.getString("given_name", null);
                    String familyName = token.getString("family_name", null);
                    
                    if (givenName != null && familyName != null) {
                        return givenName + " " + familyName;
                    } else if (givenName != null) {
                        return givenName;
                    } else if (familyName != null) {
                        return familyName;
                    }
                    return token.getString("name", null);
                });
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return httpServletRequest.getUserPrincipal() != null || parseJwtToken().isPresent();
    }

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String role) {
        return httpServletRequest.isUserInRole(role);
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Get user ID for comments (uses Keycloak sub as userId)
     */
    public Optional<String> getUserIdForComment() {
        return getKeycloakUserId();
    }

    /**
     * Get display name for comments
     */
    public String getUserNameForComment() {
        Optional<String> fullName = getFullName();
        if (fullName.isPresent()) {
            return fullName.get();
        }
        
        Optional<String> username = getUsername();
        if (username.isPresent()) {
            return username.get();
        }
        
        Optional<String> email = getEmail();
        return email.orElse("Anonymous");
    }
}
