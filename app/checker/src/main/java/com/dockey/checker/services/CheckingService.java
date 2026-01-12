package com.dockey.checker.grpc;


import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CheckingService {
        
    private static final Logger LOG = LogManager.getLogger(CheckingService.class.getName());

    public Response checkText(String text) {
        // just spits out the response for now
        LOG.info("Checking document with content: {}", text);
        """Response r = ClientBuilder.newClient()
            .target("https://zylalabs.com/api/1216/inappropriate+text+detection+api/1056/detector?text=" +
                    text)
            .request()
            .header("Authorization", "Bearer YOUR_API_KEY_HERE")
            .get(Response.class);"""
        Response r = Response.ok().entity("{\"result\":\"success\"}").build(); 
        return r;
    }



}