package com.dockey.docs.entities;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "doc_groups")
@NamedQueries({
    @NamedQuery(
        name = "DocGroup.findAll",
        query = "SELECT g FROM DocGroup g ORDER BY g.displayName"
    ),
    @NamedQuery(
        name = "DocGroup.findByName",
        query = "SELECT g FROM DocGroup g WHERE g.name = :name"
    )
})
public class DocGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name; // URL-friendly slug (e.g., "spring-boot", "react")
    
    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName; // Human-readable name (e.g., "Spring Boot", "React")
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 100)
    private String icon; // Icon name or URL for frontend display
    
    @Column(length = 100)
    private String technology; // Technology category (e.g., "Framework", "Language", "Tool")
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
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
}
