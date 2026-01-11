package com.dockey.comments.producers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class MongoDBProducer {

    private MongoClient mongoClient;

    @PostConstruct
    public void init() {
        String connectionString = System.getenv().getOrDefault("MONGODB_CONNECTION_STRING", 
            "mongodb://admin:admin@localhost:27017/commentsdb?authSource=admin");
        mongoClient = MongoClients.create(connectionString);
    }

    @Produces
    @ApplicationScoped
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    @PreDestroy
    public void cleanup() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
