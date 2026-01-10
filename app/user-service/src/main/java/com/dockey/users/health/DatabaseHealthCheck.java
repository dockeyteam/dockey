package com.dockey.users.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {
    
    @PersistenceUnit(unitName = "users-jpa-unit")
    private EntityManagerFactory emf;
    
    @Override
    public HealthCheckResponse call() {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            em.createNativeQuery("SELECT 1").getSingleResult();
            return HealthCheckResponse.up("database");
        } catch (Exception e) {
            return HealthCheckResponse.down("database");
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}
