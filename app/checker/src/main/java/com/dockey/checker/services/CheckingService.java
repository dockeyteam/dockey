package com.dockey.checker.grpc;


import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.Entity;


import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CheckingService {
        
    private static final Logger LOG = LogManager.getLogger(CheckingService.class.getName());

    public Response checkText(String text) {
        // just spits out the response for now
        LOG.info("Checking document with content: {}", text);

        // Build the JSON payload manually
        String jsonBody = String.format("{\"message\":\"%s\"}", text);

        Client client = ClientBuilder.newClient();
        Response response = client.target("https://vector.profanity.dev/")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(jsonBody, MediaType.APPLICATION_JSON));

        // Read response status and body
        int status = response.getStatus();
        String body = response.readEntity(String.class);

        LOG.info("Received response with content: {}", body);
        LOG.info("Received response with status: {}", status);

        // response.close();
        // client.close();
        return response;
    }



}