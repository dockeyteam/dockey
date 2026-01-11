package com.dockey.docs.entities;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "documents")
@NamedQueries({
    @NamedQuery(
        name = "Document.findAll",
        query = "SELECT d FROM Document d"
    ),
    @NamedQuery(
            name = "Document.findGroup",
            query = "SELECT d.id, d.title FROM Document d WHERE d.group = :group"
    ),
    @NamedQuery(
        name = "Document.findByUserId",
        query = "SELECT d FROM Document d WHERE d.userId = :userId"
    )
})
public class Document {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String group;
    
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "SRC")
    private String source;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(nullable = false)
    private String status; // DRAFT, PUBLISHED, ARCHIVED
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (status == null) {
            status = "DRAFT";
        }
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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
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
}
