package com.dockey.docs.dto;

import com.dockey.docs.entities.DocGroup;

import java.time.Instant;

public class DocGroupResponse {
    
    private Long id;
    private String name;
    private String displayName;
    private String description;
    private String icon;
    private String technology;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer documentCount; // Optional: number of documents in this group
    
    // Constructors
    
    public DocGroupResponse() {
    }
    
    public DocGroupResponse(DocGroup group) {
        this.id = group.getId();
        this.name = group.getName();
        this.displayName = group.getDisplayName();
        this.description = group.getDescription();
        this.icon = group.getIcon();
        this.technology = group.getTechnology();
        this.createdAt = group.getCreatedAt();
        this.updatedAt = group.getUpdatedAt();
    }
    
    public DocGroupResponse(DocGroup group, Integer documentCount) {
        this(group);
        this.documentCount = documentCount;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getTechnology() {
        return technology;
    }
    
    public void setTechnology(String technology) {
        this.technology = technology;
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
    
    public Integer getDocumentCount() {
        return documentCount;
    }
    
    public void setDocumentCount(Integer documentCount) {
        this.documentCount = documentCount;
    }
}
