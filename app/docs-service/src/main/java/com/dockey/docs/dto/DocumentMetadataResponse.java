package com.dockey.docs.dto;

import com.dockey.docs.entities.Document;

import java.time.Instant;

public class DocumentMetadataResponse {
    
    private Long id;
    private String title;
    private String source;
    private Long userId;
    private Long groupId;
    private String groupName;
    private String groupDisplayName;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Constructors
    
    public DocumentMetadataResponse() {
    }
    
    public DocumentMetadataResponse(Document document) {
        this.id = document.getId();
        this.title = document.getTitle();
        this.source = document.getSource();
        this.userId = document.getUserId();
        this.status = document.getStatus();
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();
        
        // Set group information if available
        if (document.getDocGroup() != null) {
            this.groupId = document.getDocGroup().getId();
            this.groupName = document.getDocGroup().getName();
            this.groupDisplayName = document.getDocGroup().getDisplayName();
        }
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
}
