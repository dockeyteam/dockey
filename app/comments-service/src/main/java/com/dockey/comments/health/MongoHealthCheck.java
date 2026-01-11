package com.dockey.comments.health;

import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Readiness
@ApplicationScoped
public class MongoHealthCheck implements HealthCheck {

    @Inject
    private MongoClient mongoClient;

    @Override
    public HealthCheckResponse call() {
        try {
            mongoClient.listDatabaseNames().first();
            return HealthCheckResponse
                .named("MongoDB connection health check")
                .up()
                .build();
        } catch (Exception e) {
            return HealthCheckResponse
                .named("MongoDB connection health check")
                .down()
                .withData("error", e.getMessage())
                .build();
        }
    }
}
