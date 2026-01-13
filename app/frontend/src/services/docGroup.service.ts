import { docsServiceApi } from './api.client';
import type {
  DocGroup,
  DocumentMetadata,
  CreateDocGroupRequest,
  UpdateDocGroupRequest,
} from '../types';

export const docGroupService = {
  /**
   * Get all document groups
   */
  async getAllGroups(): Promise<DocGroup[]> {
    const response = await docsServiceApi.get<DocGroup[]>('/doc-groups');
    return response.data;
  },

  /**
   * Get document group by ID
   */
  async getGroupById(id: number): Promise<DocGroup> {
    const response = await docsServiceApi.get<DocGroup>(`/doc-groups/${id}`);
    return response.data;
  },

  /**
   * Get document group by name (slug)
   */
  async getGroupByName(name: string): Promise<DocGroup> {
    const response = await docsServiceApi.get<DocGroup>(`/doc-groups/name/${name}`);
    return response.data;
  },

  /**
   * Get documents in a group (metadata only, no content)
   */
  async getDocumentsInGroup(groupId: number): Promise<DocumentMetadata[]> {
    const response = await docsServiceApi.get<DocumentMetadata[]>(
      `/doc-groups/${groupId}/documents`
    );
    return response.data;
  },

  /**
   * Create a new document group
   */
  async createGroup(data: CreateDocGroupRequest): Promise<DocGroup> {
    const response = await docsServiceApi.post<DocGroup>('/doc-groups', data);
    return response.data;
  },

  /**
   * Update a document group
   */
  async updateGroup(id: number, data: UpdateDocGroupRequest): Promise<DocGroup> {
    const response = await docsServiceApi.put<DocGroup>(`/doc-groups/${id}`, data);
    return response.data;
  },

  /**
   * Delete a document group
   */
  async deleteGroup(id: number): Promise<void> {
    await docsServiceApi.delete(`/doc-groups/${id}`);
  },
};
