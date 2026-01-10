package com.dockey.users.services;

import com.dockey.users.dto.UserRegistrationRequest;
import com.dockey.users.entities.User;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestScoped
public class UserService {
    
    private static final Logger LOG = LogManager.getLogger(UserService.class.getName());
    
    @Inject
    private EntityManager em;

    @Inject
    private KeycloakAdminService keycloakAdminService;
    
    public List<User> getAllUsers() {
        LOG.info("Fetching all users");
        TypedQuery<User> query = em.createNamedQuery("User.findAll", User.class);
        return query.getResultList();
    }
    
    public User getUser(Long id) {
        LOG.info("Fetching user with id: {}", id);
        return em.find(User.class, id);
    }
    
    public User getUserByEmail(String email) {
        LOG.info("Fetching user with email: {}", email);
        TypedQuery<User> query = em.createNamedQuery("User.findByEmail", User.class);
        query.setParameter("email", email);
        List<User> users = query.getResultList();
        return users.isEmpty() ? null : users.get(0);
    }
    
    public User getUserByKeycloakId(String keycloakId) {
        LOG.info("Fetching user with keycloakId: {}", keycloakId);
        TypedQuery<User> query = em.createNamedQuery("User.findByKeycloakId", User.class);
        query.setParameter("keycloakId", keycloakId);
        List<User> users = query.getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    /**
     * Login user - authenticates with Keycloak and returns tokens
     * Returns a map with "user", "accessToken", "refreshToken", and "expiresIn" keys
     */
    public Map<String, Object> loginUser(String username, String password) {
        LOG.info("Logging in user: {}", username);

        try {
            // 1. Authenticate with Keycloak and get tokens
            Map<String, Object> tokenData = keycloakAdminService.loginUser(username, password);
            
            // 2. Get user from database by username
            TypedQuery<User> query = em.createNamedQuery("User.findByUsername", User.class);
            query.setParameter("username", username);
            List<User> users = query.getResultList();
            
            if (users.isEmpty()) {
                throw new IllegalArgumentException("User not found in database");
            }
            
            User user = users.get(0);
            
            // 3. Return user and tokens
            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            result.put("accessToken", tokenData.get("accessToken"));
            result.put("refreshToken", tokenData.get("refreshToken"));
            result.put("expiresIn", tokenData.get("expiresIn"));
            
            LOG.info("User logged in successfully: {}", username);
            return result;
            
        } catch (Exception e) {
            LOG.error("Failed to login user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to login: " + e.getMessage(), e);
        }
    }

    /**
     * Register a new user - creates both in Keycloak and local database
     * Returns a map with "user" and "accessToken" keys
     */
    public Map<String, Object> registerUser(UserRegistrationRequest request) {
        LOG.info("Registering new user: {}", request.getUsername());

        // Check if user already exists in database
        User existingUser = getUserByEmail(request.getEmail());
        if (existingUser != null) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }

        // Check if username exists in Keycloak
        if (keycloakAdminService.userExistsInKeycloak(request.getUsername())) {
            throw new IllegalArgumentException("Username " + request.getUsername() + " already exists");
        }

        try {
            // 1. Create user in Keycloak first
            String keycloakId = keycloakAdminService.createKeycloakUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()
            );

            // 2. Create user in local database
            User user = new User();
            user.setKeycloakId(keycloakId);
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setRole(request.getRole() != null ? request.getRole() : "USER");

            em.getTransaction().begin();
            try {
                em.persist(user);
                em.flush();
                em.getTransaction().commit();
                
                // 3. Get tokens for the newly created user
                Map<String, Object> tokenData = keycloakAdminService.loginUser(
                    request.getUsername(),
                    request.getPassword()
                );
                
                LOG.info("User registered successfully: {}", user.getUsername());
                
                // Return user and tokens
                Map<String, Object> result = new HashMap<>();
                result.put("user", user);
                result.put("accessToken", tokenData.get("accessToken"));
                result.put("refreshToken", tokenData.get("refreshToken"));
                result.put("expiresIn", tokenData.get("expiresIn"));
                return result;
                
            } catch (Exception e) {
                em.getTransaction().rollback();
                // Rollback: delete from Keycloak if DB insert fails
                LOG.error("Failed to save user to database, rolling back Keycloak user creation");
                keycloakAdminService.deleteKeycloakUser(keycloakId);
                throw e;
            }

        } catch (Exception e) {
            LOG.error("Failed to register user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
        }
    }
    
    public User createUser(User user) {
        LOG.info("Creating new user: {}", user.getUsername());
        em.getTransaction().begin();
        try {
            em.persist(user);
            em.flush();
            em.getTransaction().commit();
            return user;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
    
    public User updateUser(Long id, User updatedUser) {
        LOG.info("Updating user with id: {}", id);
        em.getTransaction().begin();
        try {
            User user = em.find(User.class, id);
            
            if (user != null) {
                user.setUsername(updatedUser.getUsername());
                user.setEmail(updatedUser.getEmail());
                user.setFullName(updatedUser.getFullName());
                user.setRole(updatedUser.getRole());
                em.merge(user);
                em.getTransaction().commit();
                return user;
            }
            
            em.getTransaction().rollback();
            return null;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
    
    public boolean deleteUser(Long id) {
        LOG.info("Deleting user with id: {}", id);
        em.getTransaction().begin();
        try {
            User user = em.find(User.class, id);
            
            if (user != null) {
                String keycloakId = user.getKeycloakId();
                
                // Delete from database first
                em.remove(user);
                em.getTransaction().commit();
                
                // Then delete from Keycloak
                if (keycloakId != null) {
                    try {
                        keycloakAdminService.deleteKeycloakUser(keycloakId);
                    } catch (Exception e) {
                        LOG.warn("Failed to delete user from Keycloak, but DB deletion succeeded: {}", e.getMessage());
                    }
                }
                
                return true;
            }
            
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }
}
