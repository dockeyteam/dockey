package com.dockey.users;

// import org.eclipse.microprofile.auth.LoginConfig;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@OpenAPIDefinition(
    info = @Info(
        title = "Users Service API",
        version = "v1.0.0",
        contact = @Contact(
            name = "Dockey Team"
        ),
        description = "API for managing users in the Dockey system"
    ),
    servers = @Server(url = "http://localhost:8081")
)
// @LoginConfig(authMethod = "MP-JWT")  // Temporarily disabled - causing 401 on all endpoints
@ApplicationPath("/api/users")
public class UsersApplication extends Application {
}
