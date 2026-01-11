package com.dockey.docs.entities;

import javax.persistence.*;

@Entity
@Table(name = "document_line_comments", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "line_number"}))
@NamedQueries({
    @NamedQuery(
        name = "DocumentLineComment.findByDocumentId",
        query = "SELECT dlc FROM DocumentLineComment dlc WHERE dlc.documentId = :documentId ORDER BY dlc.lineNumber"
    ),
    @NamedQuery(
        name = "DocumentLineComment.findByDocumentIdAndLine",
        query = "SELECT dlc FROM DocumentLineComment dlc WHERE dlc.documentId = :documentId AND dlc.lineNumber = :lineNumber"
    ),
    @NamedQuery(
        name = "DocumentLineComment.deleteByDocumentId",
        query = "DELETE FROM DocumentLineComment dlc WHERE dlc.documentId = :documentId"
    )
})
public class DocumentLineComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "document_id", nullable = false)
    private Long documentId;
    
    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;
    
    @Column(name = "comment_count", nullable = false)
    private Integer commentCount;

    public DocumentLineComment() {
        this.commentCount = 0;
    }

    public DocumentLineComment(Long documentId, Integer lineNumber, Integer commentCount) {
        this.documentId = documentId;
        this.lineNumber = lineNumber;
        this.commentCount = commentCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }
}
