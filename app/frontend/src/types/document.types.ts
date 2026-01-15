export type DocumentStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface DocGroup {
  id: number;
  name: string; // URL-friendly slug
  displayName: string; // Human-readable name
  description?: string;
  icon?: string;
  technology?: string;
  createdAt: string;
  updatedAt?: string;
  documentCount?: number;
}

export interface Document {
  id: number;
  groupId?: number;
  groupName?: string;
  groupDisplayName?: string;
  title: string;
  source?: string;
  content: string;
  userId: number;
  status: DocumentStatus;
  createdAt: string;
  updatedAt?: string;
}

export interface DocumentMetadata {
  id: number;
  groupId?: number;
  groupName?: string;
  groupDisplayName?: string;
  title: string;
  source?: string;
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
  source?: string;
  content: string;
  groupId?: number;
  status?: DocumentStatus;
}

export interface UpdateDocumentRequest {
  title?: string;
  source?: string;
  content?: string;
  groupId?: number;
  status?: DocumentStatus;
}

export interface CreateDocGroupRequest {
  name: string;
  displayName: string;
  description?: string;
  icon?: string;
  technology?: string;
}

export interface UpdateDocGroupRequest {
  name?: string;
  displayName?: string;
  description?: string;
  icon?: string;
  technology?: string;
}

export interface DocumentLineCommentCounts {
  docId: number;
  lineCommentCounts: Record<number, number>;
}
