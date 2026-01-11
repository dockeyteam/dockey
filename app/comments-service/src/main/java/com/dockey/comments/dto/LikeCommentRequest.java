package com.dockey.comments.dto;

import javax.validation.constraints.NotNull;

public class LikeCommentRequest {
    @NotNull(message = "User ID is required")
    private String userId;

    public LikeCommentRequest() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
