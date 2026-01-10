package com.dockey.users.graphql;

import com.dockey.users.entities.User;
import com.dockey.users.services.UserService;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Name;

import javax.inject.Inject;
import java.util.List;

@GraphQLApi
public class UserGraphQLApi {
    
    @Inject
    private UserService userService;
    
    @Query("allUsers")
    @Description("Get all users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    @Query("user")
    @Description("Get user by ID")
    public User getUser(@Name("id") Long id) {
        return userService.getUser(id);
    }
    
    @Query("userByEmail")
    @Description("Get user by email")
    public User getUserByEmail(@Name("email") String email) {
        return userService.getUserByEmail(email);
    }
    
    @Mutation("createUser")
    @Description("Create a new user")
    public User createUser(@Name("input") UserInput input) {
        User user = new User();
        user.setUsername(input.getUsername());
        user.setEmail(input.getEmail());
        user.setFullName(input.getFullName());
        user.setRole(input.getRole() != null ? input.getRole() : "USER");
        return userService.createUser(user);
    }
    
    @Mutation("updateUser")
    @Description("Update an existing user")
    public User updateUser(@Name("id") Long id, @Name("input") UserInput input) {
        User user = new User();
        user.setUsername(input.getUsername());
        user.setEmail(input.getEmail());
        user.setFullName(input.getFullName());
        user.setRole(input.getRole());
        return userService.updateUser(id, user);
    }
    
    @Mutation("deleteUser")
    @Description("Delete a user")
    public Boolean deleteUser(@Name("id") Long id) {
        return userService.deleteUser(id);
    }
}
