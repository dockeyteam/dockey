package com.dockey.docs.kafka;

import java.time.LocalDateTime;

public class CommentEventMessage {
    private String eventType;
    private String commentId;
    private String docId;
    private Integer lineNumber;
    private String userId;
    private Integer newCommentCount;
    private LocalDateTime timestamp;

    public CommentEventMessage() {
    }

    // Getters and Setters
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getNewCommentCount() {
        return newCommentCount;
    }

    public void setNewCommentCount(Integer newCommentCount) {
        this.newCommentCount = newCommentCount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
