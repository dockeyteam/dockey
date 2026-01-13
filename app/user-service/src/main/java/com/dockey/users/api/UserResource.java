package com.dockey.users.api;

import com.dockey.users.dto.UserLoginRequest;
import com.dockey.users.dto.UserLoginResponse;
import com.dockey.users.dto.UserRegistrationRequest;
import com.dockey.users.dto.UserRegistrationResponse;
import com.dockey.users.dto.TokenRefreshRequest;
import com.dockey.users.dto.TokenRefreshResponse;
import com.dockey.users.entities.User;
import com.dockey.users.security.AuthenticationService;
import com.dockey.users.services.KeycloakAdminService;
import com.dockey.users.services.UserService;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestScoped
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "User management endpoints")
public class UserResource {
    
    private static final Logger LOG = LogManager.getLogger(UserResource.class.getName());
    
    @Inject
    private UserService userService;

    @Inject
    private AuthenticationService authService;

    @Inject
    private KeycloakAdminService keycloakAdminService;
    
    @GET
    @Operation(summary = "Get all users", description = "Retrieve a list of all users")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "List of users retrieved successfully",
            content = @Content(schema = @Schema(implementation = User.class))
        )
    })
    public Response getAllUsers() {
        LOG.info("GET request for all users");
        List<User> users = userService.getAllUsers();
        return Response.ok(users).build();
    }
    
    @GET
    @Path("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User retrieved successfully",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @APIResponse(responseCode = "404", description = "User not found")
    })
    public Response getUser(
        @Parameter(description = "User ID", required = true)
        @PathParam("id") Long id
    ) {
        LOG.info("GET request for user with id: {}", id);
        User user = userService.getUser(id);
        
        if (user != null) {
            return Response.ok(user).build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\": \"User not found\"}")
            .build();
    }
    
    @GET
    @Path("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieve a user by their email address")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User retrieved successfully",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @APIResponse(responseCode = "404", description = "User not found")
    })
    public Response getUserByEmail(
        @Parameter(description = "User email", required = true)
        @PathParam("email") String email
    ) {
        LOG.info("GET request for user with email: {}", email);
        User user = userService.getUserByEmail(email);
        
        if (user != null) {
            return Response.ok(user).build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\": \"User not found\"}")
            .build();
    }
    
    @GET
    @Path("/me")
    @PermitAll
    @Operation(summary = "Get current user", description = "Get the profile of the currently logged-in user")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Current user retrieved successfully",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @APIResponse(responseCode = "404", description = "User profile not found")
    })
    public Response getCurrentUser() {
        LOG.info("GET request for current user");
        
        // Try to get user from JWT token
        Optional<String> keycloakId = authService.getKeycloakUserId();
        LOG.info("Keycloak ID from token: {}", keycloakId.orElse("NOT FOUND"));
        
        Optional<User> currentUser = authService.getCurrentUser();
        
        if (currentUser.isPresent()) {
            LOG.info("User found: {}", currentUser.get().getUsername());
            return Response.ok(currentUser.get()).build();
        }
        
        LOG.warn("User not found in database. Keycloak ID: {}", keycloakId.orElse("NONE"));
        return Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\": \"User profile not found. Please complete registration.\", \"keycloakId\": \"" + keycloakId.orElse("none") + "\"}")
            .build();
    }

    @POST
    @Path("/register")
    @PermitAll
    @Operation(summary = "Register a new user", description = "Register a new user with Keycloak and create profile")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = UserRegistrationResponse.class))
        ),
        @APIResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    public Response registerUser(@Valid UserRegistrationRequest request) {
        LOG.info("POST request to register user: {}", request.getUsername());
        
        try {
            Map<String, Object> result = userService.registerUser(request);
            User user = (User) result.get("user");
            String accessToken = (String) result.get("accessToken");
            String refreshToken = (String) result.get("refreshToken");
            Integer expiresIn = (Integer) result.get("expiresIn");
            
            UserRegistrationResponse response = new UserRegistrationResponse(
                user.getId(),
                user.getKeycloakId(),
                user.getUsername(),
                user.getEmail(),
                accessToken,
                refreshToken,
                expiresIn,
                "User registered successfully"
            );
            
            return Response.status(Response.Status.CREATED).entity(response).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        } catch (Exception e) {
            LOG.error("Error registering user: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to register user: " + e.getMessage() + "\"}")
                .build();
        }
    }
    
    @POST
    @Path("/login")
    @PermitAll
    @Operation(summary = "Login user", description = "Authenticate user and return access token")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = UserLoginResponse.class))
        ),
        @APIResponse(responseCode = "401", description = "Invalid credentials")
    })
    public Response loginUser(@Valid UserLoginRequest request) {
        LOG.info("POST request to login user: {}", request.getUsername());
        
        try {
            Map<String, Object> result = userService.loginUser(request.getUsername(), request.getPassword());
            User user = (User) result.get("user");
            String accessToken = (String) result.get("accessToken");
            String refreshToken = (String) result.get("refreshToken");
            Integer expiresIn = (Integer) result.get("expiresIn");
            
            UserLoginResponse response = new UserLoginResponse(
                user.getId(),
                user.getKeycloakId(),
                user.getUsername(),
                user.getEmail(),
                accessToken,
                refreshToken,
                expiresIn
            );
            
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        } catch (Exception e) {
            LOG.error("Error logging in user: {}", e.getMessage(), e);
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"Invalid username or password\"}")
                .build();
        }
    }

    @POST
    @Path("/refresh")
    @Operation(summary = "Refresh access token", description = "Get a new access token using refresh token")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = TokenRefreshResponse.class))
        ),
        @APIResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public Response refreshToken(@Valid TokenRefreshRequest request) {
        LOG.info("POST request to refresh token");
        
        try {
            Map<String, Object> result = keycloakAdminService.refreshToken(request.getRefreshToken());
            
            TokenRefreshResponse response = new TokenRefreshResponse(
                (String) result.get("accessToken"),
                (String) result.get("refreshToken"),
                (Integer) result.get("expiresIn")
            );
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            LOG.error("Error refreshing token: {}", e.getMessage(), e);
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"Invalid or expired refresh token\"}")
                .build();
        }
    }
    
    @POST
    @Operation(summary = "Create a new user", description = "Create a new user in the system")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @APIResponse(responseCode = "400", description = "Invalid input")
    })
    public Response createUser(User user) {
        LOG.info("POST request to create user: {}", user.getUsername());
        
        if (user.getUsername() == null || user.getEmail() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Username and email are required\"}")
                .build();
        }
        
        User created = userService.createUser(user);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
    
    @PUT
    @Path("/{id}")
    @RolesAllowed({"USER", "ADMIN"})
    @Operation(summary = "Update a user", description = "Update an existing user")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @APIResponse(responseCode = "404", description = "User not found"),
        @APIResponse(responseCode = "403", description = "Forbidden - can only update own profile")
    })
    public Response updateUser(
        @Parameter(description = "User ID", required = true)
        @PathParam("id") Long id,
        User user
    ) {
        LOG.info("PUT request to update user with id: {}", id);
        
        // Check if user is trying to update their own account or is admin
        if (!authService.isAdmin() && !authService.isCurrentUser(id)) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\": \"You can only update your own profile\"}")
                .build();
        }
        
        User updated = userService.updateUser(id, user);
        
        if (updated != null) {
            return Response.ok(updated).build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\": \"User not found\"}")
            .build();
    }
    
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a user", description = "Delete a user by their ID (deletes from both DB and Keycloak)")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "User deleted successfully"),
        @APIResponse(responseCode = "404", description = "User not found"),
        @APIResponse(responseCode = "403", description = "Forbidden - cannot delete other users")
    })
    public Response deleteUser(
        @Parameter(description = "User ID", required = true)
        @PathParam("id") Long id
    ) {
        LOG.info("DELETE request for user with id: {}", id);
        
        // Check if user is trying to delete their own account or is admin
        if (!authService.isAdmin() && !authService.isCurrentUser(id)) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\": \"You can only delete your own account\"}")
                .build();
        }
        
        boolean deleted = userService.deleteUser(id);
        
        if (deleted) {
            return Response.noContent().build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\": \"User not found\"}")
            .build();
    }

    @DELETE
    @Path("/me")
    @RolesAllowed({"USER", "ADMIN"})
    @Operation(summary = "Delete current user account", description = "Delete the currently logged-in user's account")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Account deleted successfully"),
        @APIResponse(responseCode = "401", description = "Not authenticated"),
        @APIResponse(responseCode = "404", description = "User not found")
    })
    public Response deleteCurrentUser() {
        LOG.info("DELETE request for current user account");
        
        if (!authService.isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"Not authenticated\"}")
                .build();
        }

        Optional<Long> userId = authService.getCurrentUserId();
        if (userId.isPresent()) {
            boolean deleted = userService.deleteUser(userId.get());
            if (deleted) {
                return Response.noContent().build();
            }
        }
        
        return Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\": \"User not found\"}")
            .build();
    }
}
