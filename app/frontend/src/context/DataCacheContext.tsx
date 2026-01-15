import React, { createContext, useContext, useState, type ReactNode } from 'react';
import type { DocGroup, DocumentResponse } from '../types';

interface DataCacheContextType {
  // Doc Groups
  docGroups: DocGroup[] | null;
  setDocGroups: (groups: DocGroup[]) => void;
  
  // Documents by group
  groupDocuments: Map<number, any[]>;
  setGroupDocuments: (groupId: number, docs: any[]) => void;
  
  // Individual documents (with lineCommentCounts)
  documents: Map<number, DocumentResponse>;
  setDocument: (id: number, doc: DocumentResponse) => void;
  getDocument: (id: number) => DocumentResponse | undefined;
  
  // Clear cache
  clearCache: () => void;
}

const DataCacheContext = createContext<DataCacheContextType | undefined>(undefined);

export const DataCacheProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [docGroups, setDocGroupsState] = useState<DocGroup[] | null>(null);
  const [groupDocuments, setGroupDocumentsState] = useState<Map<number, any[]>>(new Map());
  const [documents, setDocumentsState] = useState<Map<number, DocumentResponse>>(new Map());

  const setDocGroups = (groups: DocGroup[]) => {
    setDocGroupsState(groups);
  };

  const setGroupDocuments = (groupId: number, docs: any[]) => {
    setGroupDocumentsState(prev => new Map(prev).set(groupId, docs));
  };

  const setDocument = (id: number, doc: DocumentResponse) => {
    setDocumentsState(prev => new Map(prev).set(id, doc));
  };

  const getDocument = (id: number): DocumentResponse | undefined => {
    return documents.get(id);
  };

  const clearCache = () => {
    setDocGroupsState(null);
    setGroupDocumentsState(new Map());
    setDocumentsState(new Map());
  };

  const value = {
    docGroups,
    setDocGroups,
    groupDocuments,
    setGroupDocuments,
    documents,
    setDocument,
    getDocument,
    clearCache,
  };

  return <DataCacheContext.Provider value={value}>{children}</DataCacheContext.Provider>;
};

export const useDataCache = () => {
  const context = useContext(DataCacheContext);
  if (!context) {
    throw new Error('useDataCache must be used within DataCacheProvider');
  }
  return context;
};
