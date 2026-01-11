package com.dockey.docs;

import com.dockey.docs.services.KafkaCommentConsumer;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.logging.Logger;
import java.util.logging.LogManager;

@ApplicationScoped
@OpenAPIDefinition(
    info = @Info(
        title = "Documents Service API",
        version = "v1.0.0",
        contact = @Contact(
            name = "Dockey Team"
        ),
        description = "API for managing documents in the Dockey system"
    ),
    servers = @Server(url = "http://localhost:8080")
)
@ApplicationPath("/v1")
public class DocsApplication extends Application {
    private static final Logger LOG = LogManager.getLogManager().getLogger(DocsApplication.class.getName());
    
    @Inject
    private KafkaCommentConsumer kafkaCommentConsumer;
    
    // Observer triggers on application startup to ensure Kafka consumer initializes
    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        LOG.info("DocsApplication observer triggered - ApplicationScoped context initialized");
        LOG.info("KafkaCommentConsumer injected: " + (kafkaCommentConsumer != null));
        // Force bean initialization by calling toString() - this triggers @PostConstruct
        if (kafkaCommentConsumer != null) {
            kafkaCommentConsumer.toString();
            LOG.info("KafkaCommentConsumer initialization triggered");
        }
    }
}
