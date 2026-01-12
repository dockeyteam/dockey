import { commentsServiceApi } from './api.client';
import type { Comment, CreateCommentRequest, LineCommentCountResponse } from '../types';

export const commentService = {
  /**
   * Create a new comment
   */
  async createComment(data: CreateCommentRequest): Promise<Comment> {
    const response = await commentsServiceApi.post<Comment>('/v1/comments', data);
    return response.data;
  },

  /**
   * Get all comments for a document
   */
  async getCommentsByDocId(docId: string, lineNumber?: number): Promise<Comment[]> {
    const params = new URLSearchParams({ docId });
    if (lineNumber !== undefined) {
      params.append('lineNumber', lineNumber.toString());
    }
    const response = await commentsServiceApi.get<Comment[]>(`/v1/comments?${params.toString()}`);
    return response.data;
  },

  /**
   * Get comments for a specific line
   */
  async getCommentsByLine(docId: string, lineNumber: number): Promise<Comment[]> {
    const response = await commentsServiceApi.get<Comment[]>(
      `/v1/comments/${docId}/line/${lineNumber}`
    );
    return response.data;
  },

  /**
   * Get line comment counts for a document
   */
  async getLineCommentCounts(docId: string): Promise<LineCommentCountResponse> {
    const response = await commentsServiceApi.get<LineCommentCountResponse>(
      `/v1/comments/${docId}/line-counts`
    );
    return response.data;
  },

  /**
   * Like a comment
   */
  async likeComment(commentId: string): Promise<Comment> {
    const response = await commentsServiceApi.post<Comment>(`/v1/comments/${commentId}/like`);
    return response.data;
  },

  /**
   * Unlike a comment
   */
  async unlikeComment(commentId: string): Promise<Comment> {
    const response = await commentsServiceApi.post<Comment>(`/v1/comments/${commentId}/unlike`);
    return response.data;
  },

  /**
   * Delete a comment
   */
  async deleteComment(commentId: string): Promise<void> {
    await commentsServiceApi.delete(`/v1/comments/${commentId}`);
  },
};
