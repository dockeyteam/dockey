package com.dockey.comments.health;

import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Readiness
@Dependent
public class MongoHealthCheck implements HealthCheck {

    @Inject
    private MongoClient mongoClient;

    @Override
    public HealthCheckResponse call() {
        try {
            // Check if CDI context is still valid and mongoClient is available
            if (mongoClient == null) {
                return HealthCheckResponse
                    .named("MongoDB connection health check")
                    .down()
                    .withData("error", "MongoClient not available")
                    .build();
            }
            mongoClient.listDatabaseNames().first();
            return HealthCheckResponse
                .named("MongoDB connection health check")
                .up()
                .build();
        } catch (Exception e) {
            // Handle any exception including CDI context shutdown
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = e.getClass().getSimpleName();
            }
            return HealthCheckResponse
                .named("MongoDB connection health check")
                .down()
                .withData("error", errorMessage)
                .build();
        }
    }
}
