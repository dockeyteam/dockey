package com.dockey.comments.services;

import com.dockey.comments.config.CommentEventMessage;
import com.dockey.comments.entities.Comment;
import com.dockey.comments.producers.KafkaCommentProducer;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CommentService {

    private static final Logger LOG = LogManager.getLogger(CommentService.class.getName());
    private static final String DATABASE_NAME = "commentsdb";
    private static final String COLLECTION_NAME = "comments";

    @Inject
    private MongoClient mongoClient;

    @Inject
    private KafkaCommentProducer kafkaProducer;

    private MongoCollection<Document> getCollection() {
        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        return database.getCollection(COLLECTION_NAME);
    }

    public Comment createComment(Comment comment) {
        try {
            MongoCollection<Document> collection = getCollection();
            
            // Store dates in UTC for consistent handling
            Document doc = new Document()
                .append("docId", comment.getDocId())
                .append("lineNumber", comment.getLineNumber())
                .append("userId", comment.getUserId())
                .append("userName", comment.getUserName())
                .append("content", comment.getContent())
                .append("createdAt", Date.from(comment.getCreatedAt().atZone(ZoneId.of("UTC")).toInstant()))
                .append("updatedAt", Date.from(comment.getUpdatedAt().atZone(ZoneId.of("UTC")).toInstant()))
                .append("likedByUserIds", comment.getLikedByUserIds())
                .append("likeCount", comment.getLikeCount())
                .append("isDeleted", comment.isDeleted());

            collection.insertOne(doc);
            comment.setId(doc.getObjectId("_id"));
            
            LOG.info("Comment created with ID: {} for docId: {} line: {}", 
                comment.getId(), comment.getDocId(), comment.getLineNumber());

            // Get updated count for this line
            int commentCount = getCommentCountForLine(comment.getDocId(), comment.getLineNumber());

            // Publish event to Kafka
            CommentEventMessage event = new CommentEventMessage(
                "COMMENT_ADDED",
                comment.getId().toString(),
                comment.getDocId(),
                comment.getLineNumber(),
                comment.getUserId(),
                commentCount
            );
            kafkaProducer.sendCommentEvent(event);

            LOG.info("Comment event published for docId: {} line: {} new count: {}", 
                comment.getDocId(), comment.getLineNumber(), commentCount);
            
            return comment;
        } catch (Exception e) {
            LOG.error("Failed to create comment", e);
            throw new RuntimeException("Failed to create comment", e);
        }
    }

    public List<Comment> getCommentsByDocId(String docId) {
        List<Comment> comments = new ArrayList<>();
        MongoCollection<Document> collection = getCollection();
        
        for (Document doc : collection.find(Filters.and(
                Filters.eq("docId", docId),
                Filters.eq("isDeleted", false)
            )).sort(Sorts.ascending("lineNumber", "createdAt"))) {
            comments.add(documentToComment(doc));
        }
        
        LOG.info("Retrieved {} comments for docId: {}", comments.size(), docId);
        return comments;
    }

    public List<Comment> getCommentsByDocIdAndLine(String docId, int lineNumber) {
        List<Comment> comments = new ArrayList<>();
        MongoCollection<Document> collection = getCollection();
        
        for (Document doc : collection.find(Filters.and(
                Filters.eq("docId", docId),
                Filters.eq("lineNumber", lineNumber),
                Filters.eq("isDeleted", false)
            )).sort(Sorts.ascending("createdAt"))) {
            comments.add(documentToComment(doc));
        }
        
        LOG.info("Retrieved {} comments for docId: {} line: {}", comments.size(), docId, lineNumber);
        return comments;
    }

    public Map<Integer, Integer> getLineCommentCounts(String docId) {
        MongoCollection<Document> collection = getCollection();
        Map<Integer, Integer> counts = new HashMap<>();
        
        for (Document doc : collection.find(Filters.and(
                Filters.eq("docId", docId),
                Filters.eq("isDeleted", false)
            ))) {
            Integer lineNumber = doc.getInteger("lineNumber");
            counts.put(lineNumber, counts.getOrDefault(lineNumber, 0) + 1);
        }
        
        LOG.info("Retrieved comment counts for {} lines in docId: {}", counts.size(), docId);
        return counts;
    }

    private int getCommentCountForLine(String docId, int lineNumber) {
        MongoCollection<Document> collection = getCollection();
        return (int) collection.countDocuments(Filters.and(
            Filters.eq("docId", docId),
            Filters.eq("lineNumber", lineNumber),
            Filters.eq("isDeleted", false)
        ));
    }

    public Comment likeComment(String commentId, String userId) {
        try {
            MongoCollection<Document> collection = getCollection();
            ObjectId id = new ObjectId(commentId);
            
            Document doc = collection.find(Filters.eq("_id", id)).first();
            if (doc == null) {
                throw new IllegalArgumentException("Comment not found: " + commentId);
            }

            @SuppressWarnings("unchecked")
            List<String> likedBy = (List<String>) doc.getList("likedByUserIds", String.class);
            
            if (likedBy.contains(userId)) {
                LOG.info("User {} already liked comment {}", userId, commentId);
                return documentToComment(doc);
            }

            // Add user to liked list and increment count
            collection.updateOne(
                Filters.eq("_id", id),
                Updates.combine(
                    Updates.addToSet("likedByUserIds", userId),
                    Updates.inc("likeCount", 1),
                    Updates.set("updatedAt", new Date())
                )
            );

            // Fetch updated document
            doc = collection.find(Filters.eq("_id", id)).first();
            Comment comment = documentToComment(doc);

            LOG.info("User {} liked comment {}", userId, commentId);

            // Publish like event
            CommentEventMessage event = new CommentEventMessage(
                "COMMENT_LIKED",
                commentId,
                comment.getDocId(),
                comment.getLineNumber(),
                userId,
                null
            );
            kafkaProducer.sendCommentEvent(event);

            return comment;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Failed to like comment", e);
            throw new RuntimeException("Failed to like comment", e);
        }
    }

    public Comment unlikeComment(String commentId, String userId) {
        try {
            MongoCollection<Document> collection = getCollection();
            ObjectId id = new ObjectId(commentId);
            
            Document doc = collection.find(Filters.eq("_id", id)).first();
            if (doc == null) {
                throw new IllegalArgumentException("Comment not found: " + commentId);
            }

            @SuppressWarnings("unchecked")
            List<String> likedBy = (List<String>) doc.getList("likedByUserIds", String.class);
            
            if (!likedBy.contains(userId)) {
                LOG.info("User {} hasn't liked comment {}", userId, commentId);
                return documentToComment(doc);
            }

            // Remove user from liked list and decrement count
            collection.updateOne(
                Filters.eq("_id", id),
                Updates.combine(
                    Updates.pull("likedByUserIds", userId),
                    Updates.inc("likeCount", -1),
                    Updates.set("updatedAt", new Date())
                )
            );

            // Fetch updated document
            doc = collection.find(Filters.eq("_id", id)).first();
            Comment comment = documentToComment(doc);

            LOG.info("User {} unliked comment {}", userId, commentId);

            // Publish unlike event
            CommentEventMessage event = new CommentEventMessage(
                "COMMENT_UNLIKED",
                commentId,
                comment.getDocId(),
                comment.getLineNumber(),
                userId,
                null
            );
            kafkaProducer.sendCommentEvent(event);

            return comment;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Failed to unlike comment", e);
            throw new RuntimeException("Failed to unlike comment", e);
        }
    }

    public boolean deleteComment(String commentId) {
        try {
            MongoCollection<Document> collection = getCollection();
            ObjectId id = new ObjectId(commentId);
            
            Document doc = collection.find(Filters.eq("_id", id)).first();
            if (doc == null) {
                return false;
            }

            Comment comment = documentToComment(doc);

            // Soft delete
            collection.updateOne(
                Filters.eq("_id", id),
                Updates.combine(
                    Updates.set("isDeleted", true),
                    Updates.set("updatedAt", new Date())
                )
            );

            LOG.info("Comment {} soft deleted", commentId);

            // Get updated count for this line
            int commentCount = getCommentCountForLine(comment.getDocId(), comment.getLineNumber());

            // Publish delete event
            CommentEventMessage event = new CommentEventMessage(
                "COMMENT_DELETED",
                commentId,
                comment.getDocId(),
                comment.getLineNumber(),
                comment.getUserId(),
                commentCount
            );
            kafkaProducer.sendCommentEvent(event);

            return true;
        } catch (Exception e) {
            LOG.error("Failed to delete comment", e);
            throw new RuntimeException("Failed to delete comment", e);
        }
    }

    private Comment documentToComment(Document doc) {
        Comment comment = new Comment();
        comment.setId(doc.getObjectId("_id"));
        comment.setDocId(doc.getString("docId"));
        comment.setLineNumber(doc.getInteger("lineNumber"));
        comment.setUserId(doc.getString("userId"));
        comment.setUserName(doc.getString("userName"));
        comment.setContent(doc.getString("content"));
        
        // Use UTC for consistent timezone handling across server/client
        Date createdAt = doc.getDate("createdAt");
        if (createdAt != null) {
            comment.setCreatedAt(LocalDateTime.ofInstant(createdAt.toInstant(), ZoneId.of("UTC")));
        }
        
        Date updatedAt = doc.getDate("updatedAt");
        if (updatedAt != null) {
            comment.setUpdatedAt(LocalDateTime.ofInstant(updatedAt.toInstant(), ZoneId.of("UTC")));
        }
        
        @SuppressWarnings("unchecked")
        List<String> likedBy = (List<String>) doc.getList("likedByUserIds", String.class);
        comment.setLikedByUserIds(likedBy);
        comment.setLikeCount(doc.getInteger("likeCount", 0));
        comment.setDeleted(doc.getBoolean("isDeleted", false));
        
        return comment;
    }
}
