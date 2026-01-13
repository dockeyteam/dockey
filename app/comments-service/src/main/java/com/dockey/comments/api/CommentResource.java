package com.dockey.comments.api;

import com.dockey.comments.dto.*;
import com.dockey.comments.security.AuthenticationService;
import com.dockey.comments.entities.Comment;
import com.dockey.comments.services.CommentService;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestScoped
@Path("/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommentResource {

    private static final Logger LOG = LogManager.getLogger(CommentResource.class.getName());

    @Inject
    private CommentService commentService;

    @Inject
    private AuthenticationService authService;

    @POST
    public Response createComment(@Valid CreateCommentRequest request) {
        try {
            // Check authentication
            if (!authService.isAuthenticated()) {
                LOG.warn("Unauthenticated attempt to create comment");
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
            }

            // Extract user info from JWT token
            String userId = authService.getUserIdForComment()
                .orElseThrow(() -> new IllegalStateException("User ID not found in token"));
            String userName = authService.getUserNameForComment();

            LOG.info("Creating comment for docId: {} line: {} by user: {} ({})", 
                request.getDocId(), request.getLineNumber(), userName, userId);

            Comment comment = new Comment(
                request.getDocId(),
                request.getLineNumber(),
                userId,
                userName,
                request.getContent()
            );

            if (!checkerClient.checkText(comment)) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Comment may contain inappropriate content\"}")
                    .build();
            }

            Comment createdComment = commentService.createComment(comment);
            CommentResponse response = toResponse(createdComment, userId);

            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (IllegalStateException e) {
            LOG.error("Authentication error: {}", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Error creating comment", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to create comment: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/doc/{docId}")
    public Response getCommentsByDocId(@PathParam("docId") String docId, 
                                       @QueryParam("userId") String userId) {
        try {
            LOG.info("Fetching all comments for docId: {}", docId);
            
            List<Comment> comments = commentService.getCommentsByDocId(docId);
            List<CommentResponse> responses = comments.stream()
                .map(comment -> toResponse(comment, userId))
                .collect(Collectors.toList());

            return Response.ok(responses).build();
        } catch (Exception e) {
            LOG.error("Error retrieving comments for docId: {}", docId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to retrieve comments: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/doc/{docId}/line/{lineNumber}")
    public Response getCommentsByDocIdAndLine(@PathParam("docId") String docId,
                                               @PathParam("lineNumber") int lineNumber,
                                               @QueryParam("userId") String userId) {
        try {
            LOG.info("Fetching comments for docId: {} line: {}", docId, lineNumber);
            
            List<Comment> comments = commentService.getCommentsByDocIdAndLine(docId, lineNumber);
            List<CommentResponse> responses = comments.stream()
                .map(comment -> toResponse(comment, userId))
                .collect(Collectors.toList());

            return Response.ok(responses).build();
        } catch (Exception e) {
            LOG.error("Error retrieving comments for docId: {} line: {}", docId, lineNumber, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to retrieve comments: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/doc/{docId}/counts")
    public Response getLineCommentCounts(@PathParam("docId") String docId) {
        try {
            LOG.info("Fetching comment counts for docId: {}", docId);
            
            Map<Integer, Integer> counts = commentService.getLineCommentCounts(docId);
            LineCommentCountResponse response = new LineCommentCountResponse(docId, counts);

            return Response.ok(response).build();
        } catch (Exception e) {
            LOG.error("Error retrieving comment counts for docId: {}", docId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to retrieve comment counts: " + e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/{commentId}/like")
    public Response likeComment(@PathParam("commentId") String commentId) {
        try {
            // Check authentication
            if (!authService.isAuthenticated()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
            }

            String userId = authService.getUserIdForComment()
                .orElseThrow(() -> new IllegalStateException("User ID not found in token"));

            LOG.info("User {} liking comment {}", userId, commentId);
            
            Comment comment = commentService.likeComment(commentId, userId);
            CommentResponse response = toResponse(comment, userId);

            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Error liking comment {}", commentId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to like comment: " + e.getMessage()))
                .build();
        }
    }

    @POST
    @Path("/{commentId}/unlike")
    public Response unlikeComment(@PathParam("commentId") String commentId) {
        try {
            // Check authentication
            if (!authService.isAuthenticated()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Authentication required"))
                    .build();
            }

            String userId = authService.getUserIdForComment()
                .orElseThrow(() -> new IllegalStateException("User ID not found in token"));

            LOG.info("User {} unliking comment {}", userId, commentId);
            
            Comment comment = commentService.unlikeComment(commentId, userId);
            CommentResponse response = toResponse(comment, userId);

            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            LOG.error("Error unliking comment {}", commentId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to unlike comment: " + e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{commentId}")
    public Response deleteComment(@PathParam("commentId") String commentId) {
        try {
            LOG.info("Deleting comment {}", commentId);
            
            boolean deleted = commentService.deleteComment(commentId);
            if (deleted) {
                return Response.ok(Map.of("message", "Comment deleted successfully")).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Comment not found"))
                    .build();
            }
        } catch (Exception e) {
            LOG.error("Error deleting comment {}", commentId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to delete comment: " + e.getMessage()))
                .build();
        }
    }

    private CommentResponse toResponse(Comment comment, String currentUserId) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId().toString());
        response.setDocId(comment.getDocId());
        response.setLineNumber(comment.getLineNumber());
        response.setUserId(comment.getUserId());
        response.setUserName(comment.getUserName());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        response.setLikeCount(comment.getLikeCount());
        response.setLikedByCurrentUser(
            currentUserId != null && comment.getLikedByUserIds().contains(currentUserId)
        );
        return response;
    }
}
