import { commentsServiceApi } from './api.client';
import type { Comment, CreateCommentRequest, LineCommentCountResponse } from '../types';

export const commentService = {
  /**
   * Create a new comment
   */
  async createComment(data: CreateCommentRequest): Promise<Comment> {
    const response = await commentsServiceApi.post<Comment>('/comments', data);
    return response.data;
  },

  /**
   * Get all comments for a document
   */
  async getCommentsByDocId(docId: string, userId?: string, lineNumber?: number): Promise<Comment[]> {
    const params = new URLSearchParams({ docId });
    if (userId) {
      params.append('userId', userId);
    }
    if (lineNumber !== undefined) {
      params.append('lineNumber', lineNumber.toString());
    }
    const response = await commentsServiceApi.get<Comment[]>(`/comments?${params.toString()}`);
    return response.data;
  },

  /**
   * Get comments for a specific line
   */
  async getCommentsByLine(docId: string, lineNumber: number, userId?: string): Promise<Comment[]> {
    const params = userId ? `?userId=${encodeURIComponent(userId)}` : '';
    const response = await commentsServiceApi.get<Comment[]>(
      `/comments/doc/${docId}/line/${lineNumber}${params}`
    );
    return response.data;
  },

  /**
   * Get line comment counts for a document
   */
  async getLineCommentCounts(docId: string): Promise<LineCommentCountResponse> {
    const response = await commentsServiceApi.get<LineCommentCountResponse>(
      `/comments/doc/${docId}/counts`
    );
    return response.data;
  },

  /**
   * Like a comment
   */
  async likeComment(commentId: string): Promise<Comment> {
    const response = await commentsServiceApi.post<Comment>(`/comments/${commentId}/like`);
    return response.data;
  },

  /**
   * Unlike a comment
   */
  async unlikeComment(commentId: string): Promise<Comment> {
    const response = await commentsServiceApi.post<Comment>(`/comments/${commentId}/unlike`);
    return response.data;
  },

  /**
   * Delete a comment
   */
  async deleteComment(commentId: string): Promise<void> {
    await commentsServiceApi.delete(`/comments/${commentId}`);
  },
};
