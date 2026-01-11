package com.dockey.comments.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateCommentRequest {
    @NotNull(message = "Document ID is required")
    private String docId;
    
    @NotNull(message = "Line number is required")
    @Min(value = 1, message = "Line number must be at least 1")
    private Integer lineNumber;
    
    @NotNull(message = "Content is required")
    @Size(min = 1, max = 5000, message = "Content must be between 1 and 5000 characters")
    private String content;

    public CreateCommentRequest() {
    }

    // Getters and Setters
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
