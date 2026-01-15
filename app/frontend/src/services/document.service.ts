import { docsServiceApi } from './api.client';
import type {
  Document,
  DocumentResponse,
  DocumentMetadata,
  CreateDocumentRequest,
  UpdateDocumentRequest,
  DocumentLineCommentCounts,
} from '../types';

export const documentService = {
  /**
   * Get all documents
   */
  async getAllDocuments(): Promise<Document[]> {
    const response = await docsServiceApi.get<Document[]>('/documents');
    return response.data;
  },

  /**
   * Get documents by group ID (metadata only, no content)
   */
  async getDocumentsByGroup(groupId: number): Promise<DocumentMetadata[]> {
    const response = await docsServiceApi.get<DocumentMetadata[]>(
      `/documents/group/${groupId}`
    );
    return response.data;
  },

  /**
   * Get document by ID with line comment counts
   */
  async getDocumentById(id: number): Promise<DocumentResponse> {
    const response = await docsServiceApi.get<DocumentResponse>(`/documents/${id}`);
    return response.data;
  },

  /**
   * Get documents by user ID
   */
  async getDocumentsByUserId(userId: number): Promise<Document[]> {
    const response = await docsServiceApi.get<Document[]>(`/documents/user/${userId}`);
    return response.data;
  },

  /**
   * Get line comment counts for a document
   */
  async getLineCommentCounts(docId: number): Promise<DocumentLineCommentCounts> {
    const response = await docsServiceApi.get<DocumentLineCommentCounts>(
      `/documents/${docId}/line-comment-counts`
    );
    return response.data;
  },

  /**
   * Create a new document
   */
  async createDocument(data: CreateDocumentRequest): Promise<Document> {
    const response = await docsServiceApi.post<Document>('/documents', data);
    return response.data;
  },

  /**
   * Update a document
   */
  async updateDocument(id: number, data: UpdateDocumentRequest): Promise<Document> {
    const response = await docsServiceApi.put<Document>(`/documents/${id}`, data);
    return response.data;
  },

  /**
   * Delete a document
   */
  async deleteDocument(id: number): Promise<void> {
    await docsServiceApi.delete(`/documents/${id}`);
  },
};
