package com.dockey.docs.api;

import com.dockey.docs.dto.DocGroupRequest;
import com.dockey.docs.dto.DocGroupResponse;
import com.dockey.docs.dto.DocumentMetadataResponse;
import com.dockey.docs.entities.DocGroup;
import com.dockey.docs.services.DocGroupService;
import com.dockey.docs.services.DocumentService;
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
import java.util.stream.Collectors;

@RequestScoped
@Path("/doc-groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Document Groups", description = "Document group management endpoints")
public class DocGroupResource {
    
    private static final Logger LOG = LogManager.getLogger(DocGroupResource.class.getName());
    
    @Inject
    private DocGroupService docGroupService;
    
    @Inject
    private DocumentService documentService;
    
    @GET
    @Operation(summary = "Get all document groups", description = "Retrieve a list of all document groups")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "List of document groups retrieved successfully",
            content = @Content(schema = @Schema(implementation = DocGroupResponse.class))
        )
    })
    public Response getAllGroups() {
        LOG.info("GET request for all document groups");
        List<DocGroup> groups = docGroupService.getAllGroups();
        
        // Map to response DTOs with document counts
        List<DocGroupResponse> responses = groups.stream()
                .map(group -> {
                    Integer docCount = docGroupService.getDocumentCountForGroup(group.getId());
                    return new DocGroupResponse(group, docCount);
                })
                .collect(Collectors.toList());
        
        return Response.ok(responses).build();
    }
    
    @GET
    @Path("/{id}")
    @Operation(summary = "Get document group by ID", description = "Retrieve a specific document group by its ID")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Document group retrieved successfully",
            content = @Content(schema = @Schema(implementation = DocGroupResponse.class))
        ),
        @APIResponse(responseCode = "404", description = "Document group not found")
    })
    public Response getGroup(
        @Parameter(description = "Document group ID", required = true)
        @PathParam("id") Long id
    ) {
        LOG.info("GET request for document group with id: {}", id);
        DocGroup group = docGroupService.getGroupById(id);
        
        if (group != null) {
            Integer docCount = docGroupService.getDocumentCountForGroup(id);
            DocGroupResponse response = new DocGroupResponse(group, docCount);
            return Response.ok(response).build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\": \"Document group not found\"}")
            .build();
    }
    
    @GET
    @Path("/name/{name}")
    @Operation(summary = "Get document group by name", description = "Retrieve a specific document group by its name (slug)")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Document group retrieved successfully",
            content = @Content(schema = @Schema(implementation = DocGroupResponse.class))
        ),
        @APIResponse(responseCode = "404", description = "Document group not found")
    })
    public Response getGroupByName(
        @Parameter(description = "Document group name (slug)", required = true)
        @PathParam("name") String name
    ) {
        LOG.info("GET request for document group with name: {}", name);
        DocGroup group = docGroupService.getGroupByName(name);
        
        if (group != null) {
            Integer docCount = docGroupService.getDocumentCountForGroup(group.getId());
            DocGroupResponse response = new DocGroupResponse(group, docCount);
            return Response.ok(response).build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\": \"Document group not found\"}")
            .build();
    }
    
    @GET
    @Path("/{id}/documents")
    @Operation(summary = "Get documents in a group", description = "Retrieve metadata for all documents in a specific group")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Document metadata list retrieved successfully",
            content = @Content(schema = @Schema(implementation = DocumentMetadataResponse.class))
        ),
        @APIResponse(responseCode = "404", description = "Document group not found")
    })
    public Response getDocumentsInGroup(
        @Parameter(description = "Document group ID", required = true)
        @PathParam("id") Long id
    ) {
        LOG.info("GET request for documents in group: {}", id);
        
        // Check if group exists
        DocGroup group = docGroupService.getGroupById(id);
        if (group == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Document group not found\"}")
                .build();
        }
        
        List<DocumentMetadataResponse> documents = documentService.getAllDocumentsByGroup(id);
        return Response.ok(documents).build();
    }
    
    @POST
    @Operation(summary = "Create a new document group", description = "Create a new document group in the system")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Document group created successfully",
            content = @Content(schema = @Schema(implementation = DocGroupResponse.class))
        ),
        @APIResponse(responseCode = "400", description = "Invalid input or group name already exists")
    })
    public Response createGroup(DocGroupRequest request) {
        LOG.info("POST request to create document group: {}", request.getDisplayName());
        
        if (request.getName() == null || request.getDisplayName() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Name and displayName are required\"}")
                .build();
        }
        
        try {
            // Convert request to entity
            DocGroup group = new DocGroup();
            group.setName(request.getName());
            group.setDisplayName(request.getDisplayName());
            group.setDescription(request.getDescription());
            group.setIcon(request.getIcon());
            group.setTechnology(request.getTechnology());
            
            DocGroup created = docGroupService.createGroup(group);
            DocGroupResponse response = new DocGroupResponse(created, 0);
            
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }
    
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a document group", description = "Update an existing document group")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Document group updated successfully",
            content = @Content(schema = @Schema(implementation = DocGroupResponse.class))
        ),
        @APIResponse(responseCode = "404", description = "Document group not found"),
        @APIResponse(responseCode = "400", description = "Invalid input or group name already exists")
    })
    public Response updateGroup(
        @Parameter(description = "Document group ID", required = true)
        @PathParam("id") Long id,
        DocGroupRequest request
    ) {
        LOG.info("PUT request to update document group with id: {}", id);
        
        try {
            // Convert request to entity
            DocGroup group = new DocGroup();
            group.setName(request.getName());
            group.setDisplayName(request.getDisplayName());
            group.setDescription(request.getDescription());
            group.setIcon(request.getIcon());
            group.setTechnology(request.getTechnology());
            
            DocGroup updated = docGroupService.updateGroup(id, group);
            
            if (updated != null) {
                Integer docCount = docGroupService.getDocumentCountForGroup(id);
                DocGroupResponse response = new DocGroupResponse(updated, docCount);
                return Response.ok(response).build();
            }
            
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Document group not found\"}")
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a document group", description = "Delete a document group by its ID. Documents in this group will have their group set to null.")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Document group deleted successfully"),
        @APIResponse(responseCode = "404", description = "Document group not found")
    })
    public Response deleteGroup(
        @Parameter(description = "Document group ID", required = true)
        @PathParam("id") Long id
    ) {
        LOG.info("DELETE request for document group with id: {}", id);
        boolean deleted = docGroupService.deleteGroup(id);
        
        if (deleted) {
            return Response.noContent().build();
        }
        
        return Response.status(Response.Status.NOT_FOUND)
            .entity("{\"error\": \"Document group not found\"}")
            .build();
    }
}
