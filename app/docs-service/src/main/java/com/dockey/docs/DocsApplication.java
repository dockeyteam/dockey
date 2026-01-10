package com.dockey.docs;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

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
}
