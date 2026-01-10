package com.dockey.users.services;

import com.dockey.users.entities.User;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Service that runs on startup to sync database with Keycloak
 */
@ApplicationScoped
public class StartupSyncService {

    private static final Logger LOG = LogManager.getLogger(StartupSyncService.class.getName());

    @PersistenceContext(unitName = "users-jpa-unit")
    private EntityManager em;

    @Inject
    private KeycloakAdminService keycloakAdminService;

    @PostConstruct
    public void init() {
        LOG.info("Running startup sync: checking for orphaned users...");
        
        try {
            // Get all users from database
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
            List<User> users = query.getResultList();

            int orphanedCount = 0;
            
            for (User user : users) {
                if (user.getKeycloakId() != null) {
                    // Check if user exists in Keycloak
                    boolean existsInKeycloak = keycloakAdminService.userExistsInKeycloakById(user.getKeycloakId());
                    
                    if (!existsInKeycloak) {
                        // User doesn't exist in Keycloak anymore - delete from database
                        LOG.info("Found orphaned user: {} (keycloakId: {}). Removing from database.", 
                                user.getUsername(), user.getKeycloakId());
                        
                        em.getTransaction().begin();
                        try {
                            User managedUser = em.find(User.class, user.getId());
                            if (managedUser != null) {
                                em.remove(managedUser);
                            }
                            em.getTransaction().commit();
                            orphanedCount++;
                        } catch (Exception e) {
                            if (em.getTransaction().isActive()) {
                                em.getTransaction().rollback();
                            }
                            LOG.error("Failed to remove orphaned user: {}", e.getMessage());
                        }
                    }
                }
            }
            
            if (orphanedCount > 0) {
                LOG.info("Startup sync complete: Removed {} orphaned user(s)", orphanedCount);
            } else {
                LOG.info("Startup sync complete: No orphaned users found");
            }
            
        } catch (Exception e) {
            LOG.error("Error during startup sync: {}", e.getMessage(), e);
        }
    }
}
