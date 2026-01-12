import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { docGroupService, documentService } from '../services';
import type { DocGroup, DocumentMetadata, Document } from '../types';

export const DocGroupDetailPage: React.FC = () => {
  const { groupName, docSlug } = useParams<{ groupName: string; docSlug?: string }>();
  const navigate = useNavigate();
  
  const [group, setGroup] = useState<DocGroup | null>(null);
  const [documents, setDocuments] = useState<DocumentMetadata[]>([]);
  const [selectedDoc, setSelectedDoc] = useState<Document | null>(null);
  const [loading, setLoading] = useState(true);
  const [docLoading, setDocLoading] = useState(false);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    if (groupName) {
      loadGroupAndDocuments(groupName);
    }
  }, [groupName]);

  useEffect(() => {
    if (docSlug && documents.length > 0) {
      // Find document by slug/title
      const doc = documents.find(d => 
        d.title.toLowerCase().replace(/\s+/g, '-') === docSlug
      );
      if (doc) {
        loadDocument(doc.id);
      }
    } else if (documents.length > 0 && !docSlug) {
      // Auto-select first document and load its content
      const firstDoc = documents[0];
      loadDocument(firstDoc.id);
      const slug = firstDoc.title.toLowerCase().replace(/\s+/g, '-');
      navigate(`/docs/${groupName}/${slug}`, { replace: true });
    }
  }, [docSlug, documents, groupName, navigate]);

  const loadGroupAndDocuments = async (name: string) => {
    try {
      setLoading(true);
      setError('');
      
      // Load group info
      const groupData = await docGroupService.getGroupByName(name);
      setGroup(groupData);
      
      // Load documents in group
      const docsData = await docGroupService.getDocumentsInGroup(groupData.id);
      setDocuments(docsData);
      
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load group');
    } finally {
      setLoading(false);
    }
  };

  const loadDocument = async (id: number) => {
    try {
      setDocLoading(true);
      const doc = await documentService.getDocumentById(id);
      setSelectedDoc(doc);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load document');
    } finally {
      setDocLoading(false);
    }
  };

  const handleDocumentSelect = (doc: DocumentMetadata) => {
    const slug = doc.title.toLowerCase().replace(/\s+/g, '-');
    navigate(`/docs/${groupName}/${slug}`);
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <span className="loading loading-spinner loading-lg"></span>
      </div>
    );
  }

  if (error && !group) {
    return (
      <div className="container mx-auto px-6 py-8">
        <div className="alert alert-error">
          <span>{error}</span>
        </div>
        <Link to="/documents" className="btn btn-primary mt-4">
          Back to Groups
        </Link>
      </div>
    );
  }

  return (
    <div className="flex h-[calc(100vh-4rem)]">
      {/* Sidebar - Documents List */}
      <div className="w-80 bg-base-200 border-r border-base-300 overflow-y-auto">
        <div className="p-4">
          {/* Back Button and Group Info */}
          <Link to="/documents" className="btn btn-ghost btn-sm mb-4">
            ← Back to Groups
          </Link>
          
          {group && (
            <div className="mb-6">
              <div className="flex items-center gap-3 mb-2">
                {group.icon && <span className="text-3xl">{group.icon}</span>}
                <h2 className="text-xl font-bold">{group.displayName}</h2>
              </div>
              {group.description && (
                <p className="text-sm text-base-content/70">{group.description}</p>
              )}
              {group.technology && (
                <div className="badge badge-outline badge-sm mt-2">{group.technology}</div>
              )}
            </div>
          )}

          <div className="divider my-2"></div>

          {/* Documents List */}
          <h3 className="font-semibold mb-3">Documents</h3>
          {documents.length === 0 ? (
            <p className="text-sm text-base-content/70">No documents yet</p>
          ) : (
            <ul className="menu menu-compact">
              {documents.map((doc) => {
                const slug = doc.title.toLowerCase().replace(/\s+/g, '-');
                const isActive = docSlug === slug;
                
                return (
                  <li key={doc.id}>
                    <button
                      onClick={() => handleDocumentSelect(doc)}
                      className={isActive ? 'active' : ''}
                    >
                      <div className="flex flex-col items-start w-full">
                        <span className="font-medium text-sm">{doc.title}</span>
                        {doc.status && (
                          <span className={`badge badge-xs ${
                            doc.status === 'PUBLISHED' ? 'badge-success' : 'badge-warning'
                          }`}>
                            {doc.status}
                          </span>
                        )}
                      </div>
                    </button>
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      </div>

      {/* Main Content - Document Content */}
      <div className="flex-1 overflow-y-auto">
        <div className="container mx-auto px-8 py-8 max-w-4xl">
          {docLoading ? (
            <div className="flex justify-center py-12">
              <span className="loading loading-spinner loading-lg"></span>
            </div>
          ) : selectedDoc ? (
            <>
              {/* Document Header */}
              <div className="mb-8">
                <h1 className="text-4xl font-bold mb-4">{selectedDoc.title}</h1>
                <div className="flex items-center gap-4 text-sm text-base-content/70">
                  {selectedDoc.source && (
                    <span className="badge badge-outline">{selectedDoc.source}</span>
                  )}
                  {selectedDoc.status && (
                    <span className={`badge ${
                      selectedDoc.status === 'PUBLISHED' ? 'badge-success' : 'badge-warning'
                    }`}>
                      {selectedDoc.status}
                    </span>
                  )}
                  {selectedDoc.createdAt && (
                    <span>Created: {new Date(selectedDoc.createdAt).toLocaleDateString()}</span>
                  )}
                </div>
              </div>

              {/* Document Content */}
              <div className="prose prose-lg max-w-none">
                {selectedDoc.content ? (
                  <div dangerouslySetInnerHTML={{ __html: selectedDoc.content }} />
                ) : (
                  <p className="text-base-content/70">No content available</p>
                )}
              </div>
            </>
          ) : (
            <div className="text-center py-12">
              <p className="text-lg text-base-content/70">
                Select a document from the sidebar
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
