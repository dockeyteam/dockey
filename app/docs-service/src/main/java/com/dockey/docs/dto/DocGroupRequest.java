package com.dockey.docs.dto;

public class DocGroupRequest {
    
    private String name; // URL-friendly slug
    private String displayName; // Human-readable name
    private String description;
    private String icon;
    private String technology;
    
    // Constructors
    
    public DocGroupRequest() {
    }
    
    public DocGroupRequest(String name, String displayName, String description, String icon, String technology) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.technology = technology;
    }
    
    // Getters and Setters
    
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
}
