package com.dockey.docs.api;

import com.dockey.docs.dto.DocumentResponse;
import com.dockey.docs.entities.Document;
import com.dockey.docs.services.DocumentService;
import com.dockey.docs.services.DocumentLineCommentService;
import com.dockey.docs.services.KafkaCommentConsumer;
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
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@RequestScoped
@Path("/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Documents", description = "Document management endpoints")
public class DocumentResource {
    
    private static final Logger LOG = LogManager.getLogger(DocumentResource.class.getName());
    
    @Inject
    private DocumentService documentService;
    
    @Inject
    private DocumentLineCommentService documentLineCommentService;
    
    // Injecting to ensure Kafka consumer starts at application startup
    @Inject
    private KafkaCommentConsumer kafkaCommentConsumer;
    
    @GET
    @Operation(summary = "Get all documents", description = "Retrieve a list of all documents")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "List of documents retrieved successfully",
            content = @Content(schema = @Schema(implementation = Document.class))
        )
    })
    public Response getAllDocuments() {
        LOG.info("GET request for all documents");
        List<Document> documents = documentService.getAllDocuments();
        return Response.ok(documents).build();
    }

    @GET
    @Path("/group/{group}")
    @Operation(summary = "Get all documents from a group", description = "Retrieve a list of all documents belonging to a specific group")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Group of documents retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Object.class))
            )
    })
    public Response getAllDocumentsByGroup(
            @Parameter(description = "Document group name", required = true)
            @PathParam("group") String group
    ) {
        LOG.info("GET request for all documents in a certain group");
        List<Object> documents = documentService.getAllDocumentsByGroup(group);
        return Response.ok(documents).build();
    }
    
    @GET
    @Path("/{id}")
    @Operation(summary = "Get document by ID", description = "Retrieve a specific document by its ID with line comment counts")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Document retrieved successfully",
            content = @Content(schema = @Schema(implementation = DocumentResponse.class))
        ),
        @APIResponse(responseCode = "404", description = "Document not found")
    })
    public Response getDocument(
        @Parameter(description = "Document ID", required = true)
        @PathParam("id") Long id
    ) {
        LOG.info("GET request for document with id: {}", id);
        Document document = documentService.getDocument(id);
        
        if (document != null) {
            // Fetch line comment counts
            Map<Integer, Integer> lineCommentCounts = documentLineCommentService.getLineCommentCountsMap(id);
            DocumentResponse response = new DocumentResponse(document, lineCommentCounts);
            return Response.ok(response).build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\": \"Document not found\"}")
            .build();
    }
    
    @GET
    @Path("/user/{userId}")
    @Operation(summary = "Get documents by user ID", description = "Retrieve all documents for a specific user")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Documents retrieved successfully",
            content = @Content(schema = @Schema(implementation = Document.class))
        )
    })
    public Response getDocumentsByUser(
        @Parameter(description = "User ID", required = true)
        @PathParam("userId") Long userId
    ) {
        LOG.info("GET request for documents by user: {}", userId);
        List<Document> documents = documentService.getDocumentsByUserId(userId);
        return Response.ok(documents).build();
    }
    
    @POST
    @Operation(summary = "Create a new document", description = "Create a new document in the system")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Document created successfully",
            content = @Content(schema = @Schema(implementation = Document.class))
        ),
        @APIResponse(responseCode = "400", description = "Invalid input")
    })
    public Response createDocument(Document document) {
        LOG.info("POST request to create document: {}", document.getTitle());
        
        if (document.getTitle() == null || document.getUserId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Title and userId are required\"}")
                .build();
        }
        
        Document created = documentService.createDocument(document);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
    
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a document", description = "Update an existing document")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Document updated successfully",
            content = @Content(schema = @Schema(implementation = Document.class))
        ),
        @APIResponse(responseCode = "404", description = "Document not found")
    })
    public Response updateDocument(
        @Parameter(description = "Document ID", required = true)
        @PathParam("id") Long id,
        Document document
    ) {
        LOG.info("PUT request to update document with id: {}", id);
        Document updated = documentService.updateDocument(id, document);
        
        if (updated != null) {
            return Response.ok(updated).build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\": \"Document not found\"}")
            .build();
    }
    
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a document", description = "Delete a document by its ID")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Document deleted successfully"),
        @APIResponse(responseCode = "404", description = "Document not found")
    })
    public Response deleteDocument(
        @Parameter(description = "Document ID", required = true)
        @PathParam("id") Long id
    ) {
        LOG.info("DELETE request for document with id: {}", id);
        boolean deleted = documentService.deleteDocument(id);
        
        if (deleted) {
            return Response.noContent().build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\": \"Document not found\"}")
            .build();
    }
    
    @GET
    @Path("/{id}/line-comments")
    @Operation(summary = "Get line comment counts", description = "Get comment counts per line for a document")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Line comment counts retrieved successfully"
        ),
        @APIResponse(responseCode = "404", description = "Document not found")
    })
    public Response getLineCommentCounts(
        @Parameter(description = "Document ID", required = true)
        @PathParam("id") Long id
    ) {
        LOG.info("GET request for line comment counts for document: {}", id);
        
        // Check if document exists
        Document document = documentService.getDocument(id);
        if (document == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Document not found\"}")
                .build();
        }
        
        Map<Integer, Integer> lineCommentCounts = documentLineCommentService.getLineCommentCountsMap(id);
        return Response.ok(Map.of(
            "documentId", id,
            "lineCommentCounts", lineCommentCounts
        )).build();
    }
}
