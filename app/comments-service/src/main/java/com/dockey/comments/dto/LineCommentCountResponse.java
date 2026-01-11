package com.dockey.comments.dto;

import java.util.Map;

public class LineCommentCountResponse {
    private String docId;
    private Map<Integer, Integer> lineCommentCounts;

    public LineCommentCountResponse() {
    }

    public LineCommentCountResponse(String docId, Map<Integer, Integer> lineCommentCounts) {
        this.docId = docId;
        this.lineCommentCounts = lineCommentCounts;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public Map<Integer, Integer> getLineCommentCounts() {
        return lineCommentCounts;
    }

    public void setLineCommentCounts(Map<Integer, Integer> lineCommentCounts) {
        this.lineCommentCounts = lineCommentCounts;
    }
}
