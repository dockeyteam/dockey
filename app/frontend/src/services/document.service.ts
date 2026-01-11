import { docsServiceApi } from './api.client';
import type {
  Document,
  DocumentResponse,
  CreateDocumentRequest,
  UpdateDocumentRequest,
  LineCommentCountResponse,
} from '../types';

export const documentService = {
  /**
   * Get all documents
   */
  async getAllDocuments(): Promise<Document[]> {
    const response = await docsServiceApi.get<Document[]>('/v1/documents');
    return response.data;
  },

  /**
   * Get document by ID with line comment counts
   */
  async getDocumentById(id: number): Promise<DocumentResponse> {
    const response = await docsServiceApi.get<DocumentResponse>(`/v1/documents/${id}`);
    return response.data;
  },

  /**
   * Get documents by user ID
   */
  async getDocumentsByUserId(userId: number): Promise<Document[]> {
    const response = await docsServiceApi.get<Document[]>(`/v1/documents/user/${userId}`);
    return response.data;
  },

  /**
   * Get line comment counts for a document
   */
  async getLineCommentCounts(docId: number): Promise<LineCommentCountResponse> {
    const response = await docsServiceApi.get<LineCommentCountResponse>(
      `/v1/documents/${docId}/line-comment-counts`
    );
    return response.data;
  },

  /**
   * Create a new document
   */
  async createDocument(data: CreateDocumentRequest): Promise<Document> {
    const response = await docsServiceApi.post<Document>('/v1/documents', data);
    return response.data;
  },

  /**
   * Update a document
   */
  async updateDocument(id: number, data: UpdateDocumentRequest): Promise<Document> {
    const response = await docsServiceApi.put<Document>(`/v1/documents/${id}`, data);
    return response.data;
  },

  /**
   * Delete a document
   */
  async deleteDocument(id: number): Promise<void> {
    await docsServiceApi.delete(`/v1/documents/${id}`);
  },
};
