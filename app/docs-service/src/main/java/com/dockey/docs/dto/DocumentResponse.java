package com.dockey.docs.dto;

import com.dockey.docs.entities.Document;

import java.time.Instant;
import java.util.Map;

public class DocumentResponse {
    private Long id;
    private Long groupId;
    private String groupName;
    private String groupDisplayName;
    private String title;
    private String source;
    private String content;
    private Long userId;
    private Instant createdAt;
    private Instant updatedAt;
    private String status;
    private Map<Integer, Integer> lineCommentCounts;

    public DocumentResponse() {
    }

    public DocumentResponse(Document document, Map<Integer, Integer> lineCommentCounts) {
        this.id = document.getId();
        
        // Map group information if available
        if (document.getDocGroup() != null) {
            this.groupId = document.getDocGroup().getId();
            this.groupName = document.getDocGroup().getName();
            this.groupDisplayName = document.getDocGroup().getDisplayName();
        }
        
        this.title = document.getTitle();
        this.source = document.getSource();
        this.content = document.getContent();
        this.userId = document.getUserId();
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();
        this.status = document.getStatus();
        this.lineCommentCounts = lineCommentCounts;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDisplayName() {
        return groupDisplayName;
    }

    public void setGroupDisplayName(String groupDisplayName) {
        this.groupDisplayName = groupDisplayName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setource(String source) {
        this.source = source;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<Integer, Integer> getLineCommentCounts() {
        return lineCommentCounts;
    }

    public void setLineCommentCounts(Map<Integer, Integer> lineCommentCounts) {
        this.lineCommentCounts = lineCommentCounts;
    }
}
