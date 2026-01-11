export type DocumentStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface Document {
  id: number;
  title: string;
  content: string;
  userId: number;
  status: DocumentStatus;
  createdAt: string;
  updatedAt?: string;
}

export interface DocumentResponse extends Document {
  lineCommentCounts?: Record<number, number>;
}

export interface CreateDocumentRequest {
  title: string;
  content: string;
  status?: DocumentStatus;
}

export interface UpdateDocumentRequest {
  title?: string;
  content?: string;
  status?: DocumentStatus;
}

export interface LineCommentCountResponse {
  docId: number;
  lineCommentCounts: Record<number, number>;
}
