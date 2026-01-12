package com.dockey.checker.grpc;


import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.dockey.checker.grpc.Doc;
import com.dockey.checker.grpc.Comm;
import com.dockey.checker.grpc.Check;
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

    public Response checkDocument(String text) {
        LOG.info("Checking document with content: {}", text);
        Response r = ClientBuilder.newClient()
            .target("https://zylalabs.com/api/1216/inappropriate+text+detection+api/1056/detector?text=" +
                    "lalalalala" + "&level=1")
            .request()
            .header("Authorization", "Bearer YOUR_API_KEY_HERE")
            .get(Response.class);
        return r; // true; //placeholder
    }


    public Response checkComment(String text) {
        LOG.info("Checking comment with content: {}", text);
        Response r = ClientBuilder.newClient()
            .target("https://zylalabs.com/api/1216/inappropriate+text+detection+api/1056/detector?text=" +
                    "lalalalala" + "&level=1")
            .request()
            .header("Authorization", "Bearer YOUR_API_KEY_HERE")
            .get(Response.class);
        return r; // true; //placeholder
    }



}