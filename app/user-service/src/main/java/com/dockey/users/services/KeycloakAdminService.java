package com.dockey.users.services;

import com.dockey.users.config.KeycloakConfig;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing Keycloak users via REST Admin API
 */
@ApplicationScoped
public class KeycloakAdminService {

    private static final Logger LOG = LogManager.getLogger(KeycloakAdminService.class.getName());

    @Inject
    private KeycloakConfig keycloakConfig;

    private Client client = ClientBuilder.newClient();

    /**
     * Get admin access token
     */
    private String getAdminToken() {
        Form form = new Form();
        form.param("grant_type", "password");
        form.param("client_id", "admin-cli");
        form.param("username", keycloakConfig.getAdminUsername());
        form.param("password", keycloakConfig.getAdminPassword());

        Response response = client.target(keycloakConfig.getAuthServerUrl())
                .path("realms/master/protocol/openid-connect/token")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.form(form));

        String jsonResponse = response.readEntity(String.class);
        response.close();

        JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse));
        JsonObject tokenObject = jsonReader.readObject();
        return tokenObject.getString("access_token");
    }

    /**
     * Create a user in Keycloak
     * @return Keycloak user ID
     */
    public String createKeycloakUser(String username, String email, String password, 
                                      String firstName, String lastName) {
        LOG.info("Creating user in Keycloak: {}", username);
        
        try {
            String adminToken = getAdminToken();

            // Create user JSON
            JsonObject userJson = Json.createObjectBuilder()
                    .add("username", username)
                    .add("email", email)
                    .add("firstName", firstName != null ? firstName : "")
                    .add("lastName", lastName != null ? lastName : "")
                    .add("enabled", true)
                    .add("emailVerified", true)
                    .build();

            Response response = client.target(keycloakConfig.getAuthServerUrl())
                    .path("admin/realms/" + keycloakConfig.getRealm() + "/users")
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .post(Entity.json(userJson.toString()));

            if (response.getStatus() != 201) {
                String errorMsg = response.readEntity(String.class);
                response.close();
                LOG.error("Failed to create user in Keycloak. Status: {}, Error: {}", 
                         response.getStatus(), errorMsg);
                throw new RuntimeException("Failed to create user in Keycloak: " + errorMsg);
            }

            // Extract user ID from location header
            String locationHeader = response.getHeaderString("Location");
            response.close();
            String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

            // Set password
            setUserPassword(userId, password, false);

            LOG.info("Successfully created user in Keycloak with ID: {}", userId);
            return userId;

        } catch (Exception e) {
            LOG.error("Error creating user in Keycloak: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user in Keycloak", e);
        }
    }

    /**
     * Get user access token (login user)
     * Returns a map with accessToken, refreshToken, and expiresIn
     */
    public Map<String, Object> loginUser(String username, String password) {
        LOG.info("Logging in user: {}", username);
        
        try {
            Form form = new Form();
            form.param("grant_type", "password");
            form.param("client_id", "dockey-user-service");
            form.param("client_secret", "dockey-user-service-secret-key-123");
            form.param("username", username);
            form.param("password", password);

            Response response = client.target(keycloakConfig.getAuthServerUrl())
                    .path("realms/" + keycloakConfig.getRealm() + "/protocol/openid-connect/token")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.form(form));

            if (response.getStatus() != 200) {
                String errorMsg = response.readEntity(String.class);
                response.close();
                LOG.error("Failed to login. Status: {}, Error: {}", 
                         response.getStatus(), errorMsg);
                throw new RuntimeException("Invalid username or password");
            }

            String jsonResponse = response.readEntity(String.class);
            response.close();

            JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse));
            JsonObject tokenObject = jsonReader.readObject();
            
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", tokenObject.getString("access_token"));
            result.put("refreshToken", tokenObject.getString("refresh_token", null));
            result.put("expiresIn", tokenObject.getInt("expires_in", 300));
            
            return result;
            
        } catch (Exception e) {
            LOG.error("Error logging in: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to login: " + e.getMessage(), e);
        }
    }

    /**
     * Get user access token (simple version for registration)
     */
    public String getUserAccessToken(String username, String password) {
        Map<String, Object> result = loginUser(username, password);
        return (String) result.get("accessToken");
    }

    /**
     * Refresh access token using refresh token
     * Returns a map with accessToken, refreshToken, and expiresIn
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        LOG.info("Refreshing access token");
        
        try {
            Form form = new Form();
            form.param("grant_type", "refresh_token");
            form.param("client_id", "dockey-user-service");
            form.param("client_secret", "dockey-user-service-secret-key-123");
            form.param("refresh_token", refreshToken);

            Response response = client.target(keycloakConfig.getAuthServerUrl())
                    .path("realms/" + keycloakConfig.getRealm() + "/protocol/openid-connect/token")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.form(form));

            if (response.getStatus() != 200) {
                String errorMsg = response.readEntity(String.class);
                response.close();
                LOG.error("Failed to refresh token. Status: {}, Error: {}", 
                         response.getStatus(), errorMsg);
                throw new RuntimeException("Invalid or expired refresh token");
            }

            String jsonResponse = response.readEntity(String.class);
            response.close();

            JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse));
            JsonObject tokenObject = jsonReader.readObject();
            
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", tokenObject.getString("access_token"));
            result.put("refreshToken", tokenObject.getString("refresh_token", null));
            result.put("expiresIn", tokenObject.getInt("expires_in", 300));
            
            return result;
            
        } catch (Exception e) {
            LOG.error("Error refreshing token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to refresh token: " + e.getMessage(), e);
        }
    }

    /**
     * Set/reset user password in Keycloak
     */
    public void setUserPassword(String keycloakUserId, String password, boolean temporary) {
        LOG.info("Setting password for Keycloak user: {}", keycloakUserId);
        
        try {
            String adminToken = getAdminToken();

            JsonObject credentialJson = Json.createObjectBuilder()
                    .add("type", "password")
                    .add("value", password)
                    .add("temporary", temporary)
                    .build();

            Response response = client.target(keycloakConfig.getAuthServerUrl())
                    .path("admin/realms/" + keycloakConfig.getRealm() + "/users/" + keycloakUserId + "/reset-password")
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .put(Entity.json(credentialJson.toString()));

            int status = response.getStatus();
            response.close();

            if (status >= 200 && status < 300) {
                LOG.info("Password set successfully for user: {}", keycloakUserId);
            } else {
                throw new RuntimeException("Failed to set password, status: " + status);
            }

        } catch (Exception e) {
            LOG.error("Error setting password for user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to set user password in Keycloak", e);
        }
    }

    /**
     * Delete user from Keycloak
     */
    public void deleteKeycloakUser(String keycloakUserId) {
        LOG.info("Deleting user from Keycloak: {}", keycloakUserId);
        
        try {
            String adminToken = getAdminToken();

            Response response = client.target(keycloakConfig.getAuthServerUrl())
                    .path("admin/realms/" + keycloakConfig.getRealm() + "/users/" + keycloakUserId)
                    .request()
                    .header("Authorization", "Bearer " + adminToken)
                    .delete();

            int status = response.getStatus();
            response.close();

            if (status >= 200 && status < 300) {
                LOG.info("Successfully deleted user from Keycloak: {}", keycloakUserId);
            } else {
                LOG.warn("Failed to delete user from Keycloak. Status: {}", status);
            }

        } catch (Exception e) {
            LOG.error("Error deleting user from Keycloak: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete user from Keycloak", e);
        }
    }

    /**
     * Update user in Keycloak
     */
    public void updateKeycloakUser(String keycloakUserId, String username, String email, 
                                     String firstName, String lastName) {
        LOG.info("Updating user in Keycloak: {}", keycloakUserId);
        
        try {
            String adminToken = getAdminToken();

            JsonObject userJson = Json.createObjectBuilder()
                    .add("username", username)
                    .add("email", email)
                    .add("firstName", firstName != null ? firstName : "")
                    .add("lastName", lastName != null ? lastName : "")
                    .build();

            Response response = client.target(keycloakConfig.getAuthServerUrl())
                    .path("admin/realms/" + keycloakConfig.getRealm() + "/users/" + keycloakUserId)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .put(Entity.json(userJson.toString()));

            int status = response.getStatus();
            response.close();

            if (status >= 200 && status < 300) {
                LOG.info("Successfully updated user in Keycloak: {}", keycloakUserId);
            } else {
                throw new RuntimeException("Failed to update user, status: " + status);
            }

        } catch (Exception e) {
            LOG.error("Error updating user in Keycloak: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update user in Keycloak", e);
        }
    }

    /**
     * Check if user exists in Keycloak by username
     */
    public boolean userExistsInKeycloak(String username) {
        try {
            String adminToken = getAdminToken();

            Response response = client.target(keycloakConfig.getAuthServerUrl())
                    .path("admin/realms/" + keycloakConfig.getRealm() + "/users")
                    .queryParam("username", username)
                    .queryParam("exact", "true")
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .get();

            String jsonResponse = response.readEntity(String.class);
            response.close();

            JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse));
            JsonArray users = jsonReader.readArray();
            
            return !users.isEmpty();

        } catch (Exception e) {
            LOG.error("Error checking if user exists in Keycloak: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if user exists in Keycloak by Keycloak ID
     */
    public boolean userExistsInKeycloakById(String keycloakId) {
        try {
            String adminToken = getAdminToken();

            Response response = client.target(keycloakConfig.getAuthServerUrl())
                    .path("admin/realms/" + keycloakConfig.getRealm() + "/users/" + keycloakId)
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .get();

            int status = response.getStatus();
            response.close();
            
            // 200 = user exists, 404 = user not found
            return status == 200;

        } catch (Exception e) {
            LOG.error("Error checking if user exists in Keycloak by ID: {}", e.getMessage(), e);
            return false;
        }
    }
}
